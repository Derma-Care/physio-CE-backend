package com.clinicadmin.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortIncompleteMultipartUpload;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {
	
	

    // ─────────────────────────────────────────────
    // Used ONLY by the legacy base64 upload flow
    // ─────────────────────────────────────────────
    private static final int MAX_LEGACY_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // ─────────────────────────────────────────────
    // Presigned URL expiry constants
    // ─────────────────────────────────────────────
    private static final Duration PUT_URL_EXPIRY = Duration.ofMinutes(15); // upload window
    private static final Duration GET_URL_EXPIRY = Duration.ofHours(1);    // ← default signed-url expiry used elsewhere

    // ─────────────────────────────────────────────
    // NEW: WhatsApp-share-specific constants
    // Kept separate from GET_URL_EXPIRY above so this
    // change can't accidentally affect other flows.
    // ─────────────────────────────────────────────
    private static final Duration WHATSAPP_SHARE_URL_EXPIRY = Duration.ofDays(3); // ← link valid for 3 days
    private static final String   WHATSAPP_SHARE_FOLDER     = "whatsapp-shares";  // ← dedicated prefix for lifecycle targeting
    private static final int      WHATSAPP_SHARE_LIFECYCLE_DAYS = 3;              // ← object auto-deletes from S3 after this

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            // images
            "jpg", "jpeg", "png", "webp",
            // videos
            "mp4", "mov", "avi", "mkv", "webm",
            // audio
            "mp3", "wav", "ogg", "m4a", "aac",
            // docs
            "pdf", "doc", "docx"
    );

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Autowired
    private S3Presigner s3Presigner;

    // ─────────────────────────────────────────────
    // NEW FLOW (Step 1): Generate presigned PUT URL
    // → frontend uploads directly to S3
    //
    // Backward-compatible overload: existing callers using
    // generatePresignedPutUrl(folder, extension) are unaffected —
    // they get the original bare-UUID key behavior.
    //
    // Callers that pass an originalFileName (e.g. the WhatsApp-share
    // flow) get a MEANINGFUL key instead, e.g.:
    //   whatsapp-shares/invoice-march-2026-a1b2c3.pdf
    // instead of:
    //   whatsapp-shares/8f14e45f-...-b9a1.pdf
    // Still guaranteed-unique (random suffix appended), but readable.
    // ─────────────────────────────────────────────
    public Map<String, String> generatePresignedPutUrl(String folder, String extension) {
        return generatePresignedPutUrl(folder, extension, null);
    }

    public Map<String, String> generatePresignedPutUrl(String folder, String extension, String originalFileName) {

        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("File extension must not be null or blank");
        }

        String ext = extension.toLowerCase().trim();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                "File extension '." + ext + "' is not allowed. Allowed: " + ALLOWED_EXTENSIONS
            );
        }
        String contentType = resolveContentType(ext);

        String fileKey = (originalFileName == null || originalFileName.isBlank())
                ? folder + "/" + UUID.randomUUID() + "." + ext                       // original behavior, untouched
                : folder + "/" + buildMeaningfulFileName(originalFileName, ext);      // meaningful key

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PUT_URL_EXPIRY)
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        return Map.of(
                "uploadUrl",   uploadUrl,
                "fileKey",     fileKey,
                "contentType", contentType
        );
    }

    // ─────────────────────────────────────────────
    // NEW FLOW (Step 2): Generate signed GET URL — DEFAULT expiry (1 hour)
    // Used by existing flows. Left untouched.
    // ─────────────────────────────────────────────
    public String generateSignedUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name must not be null or blank");
        }
        return doPresignGet(fileName, GET_URL_EXPIRY);
    }

    // ─────────────────────────────────────────────
    // NEW: Generate signed GET URL for WhatsApp-share flow — 3-day expiry
    //
    // Shares the same doPresignGet(...) logic as generateSignedUrl above
    // (no duplicated presign code) — only the expiry duration differs.
    //
    // NOTE: SigV4 presigned URLs cap out at 7 days, so 3 days is fine —
    // but ONLY if the underlying S3Presigner/S3Client credentials are
    // long-term IAM user keys. If your app assumes an IAM role (STS
    // temporary credentials), the presigned URL's actual validity is
    // capped at whatever's left on that STS session, regardless of what
    // you request here. Worth double-checking how the S3Presigner bean
    // is configured before relying on the full 3 days in production.
    // ─────────────────────────────────────────────
    public String generateWhatsAppShareSignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("File key must not be null or blank");
        }
        return doPresignGet(fileKey, WHATSAPP_SHARE_URL_EXPIRY);
    }

    // ─────────────────────────────────────────────
    // SHARED HELPER: actual presign-GET logic, used by both
    // generateSignedUrl and generateWhatsAppShareSignedUrl.
    // ─────────────────────────────────────────────
    private String doPresignGet(String fileKey, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }

    // ─────────────────────────────────────────────
    // Fetch real MIME type + size from S3 via a single HeadObject call.
    // Returns null if the file does not exist.
    // ─────────────────────────────────────────────
    public Map<String, Object> getUploadedFileMeta(String fileKey) {

        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("File key must not be null or blank");
        }

        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            HeadObjectResponse metadata = s3Client.headObject(headRequest);

            return Map.of(
                    "contentType",    metadata.contentType(),
                    "contentLength",  metadata.contentLength(),
                    "isEncrypted",    metadata.serverSideEncryption() != null,
                    "encryptionType", metadata.serverSideEncryptionAsString() != null
                                      ? metadata.serverSideEncryptionAsString()
                                      : "NONE"
            );

        } catch (NoSuchKeyException e) {
            return null;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch metadata from S3 for key: " + fileKey, e
            );
        }
    }

    // ─────────────────────────────────────────────
    // NEW: One-time (idempotent) S3 Lifecycle rule setup.
    //
    // Scopes an "expire after 3 days" rule to the whatsapp-shares/
    // prefix ONLY — nothing else in the bucket is affected. Calling
    // this repeatedly just re-applies the same rule; it's safe to run
    // on every app startup.
    //
    // This is what makes the file "not stored": S3 deletes the object
    // itself after WHATSAPP_SHARE_LIFECYCLE_DAYS, independent of any
    // app-level cleanup job or DB record.
    // ─────────────────────────────────────────────
    @PostConstruct
    public void ensureWhatsAppShareLifecycleRule() {
        try {
            LifecycleRuleFilter filter = LifecycleRuleFilter.builder()
                    .prefix(WHATSAPP_SHARE_FOLDER + "/")
                    .build();

            LifecycleRule rule = LifecycleRule.builder()
                    .id("expire-whatsapp-shares-after-3-days")
                    .status(ExpirationStatus.ENABLED)
                    .filter(filter)
                    .expiration(LifecycleExpiration.builder()
                            .days(WHATSAPP_SHARE_LIFECYCLE_DAYS)
                            .build())
                    .build();

            BucketLifecycleConfiguration lifecycleConfig = BucketLifecycleConfiguration.builder()
                    .rules(List.of(rule))
                    .build();

            s3Client.putBucketLifecycleConfiguration(
                    PutBucketLifecycleConfigurationRequest.builder()
                            .bucket(bucketName)
                            .lifecycleConfiguration(lifecycleConfig)
                            .build()
            );

        } catch (Exception e) {
            // Don't crash app startup over this — but surface it loudly,
            // since silent failure here means files never get cleaned up.
            System.err.println(
                    "WARNING: Failed to apply whatsapp-shares S3 lifecycle rule. "
                  + "Shared PDFs will NOT auto-delete after " + WHATSAPP_SHARE_LIFECYCLE_DAYS
                  + " days until this is fixed. Cause: " + e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────────
    // SHARED HELPER: build a meaningful, still-unique file name
    // e.g. "Patient Invoice March.pdf" → "patient-invoice-march-a1b2c3.pdf"
    // ─────────────────────────────────────────────
    private String buildMeaningfulFileName(String originalFileName, String ext) {
        String base = (originalFileName == null || originalFileName.isBlank())
                ? "shared-file"
                : originalFileName;

        // strip extension if caller included it
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }

        // sanitize: lowercase, spaces/underscores → dashes, strip anything
        // that isn't alphanumeric or a dash, collapse repeats, trim length
        String sanitized = base.trim()
                .toLowerCase()
                .replaceAll("[\\s_]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        if (sanitized.isBlank()) {
            sanitized = "shared-file";
        }
        if (sanitized.length() > 60) {
            sanitized = sanitized.substring(0, 60);
        }

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        return sanitized + "-" + uniqueSuffix + "." + ext;
    }

    // ─────────────────────────────────────────────
    // SHARED HELPER: extension → Content-Type
    // ─────────────────────────────────────────────
    private String resolveContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"         -> "image/png";
            case "webp"        -> "image/webp";
            case "mp4"         -> "video/mp4";
            case "mov"         -> "video/quicktime";
            case "avi"         -> "video/x-msvideo";
            case "mkv"         -> "video/x-matroska";
            case "webm"        -> "video/webm";
            case "mp3"         -> "audio/mpeg";
            case "wav"         -> "audio/wav";
            case "ogg"         -> "audio/ogg";
            case "m4a"         -> "audio/mp4";
            case "aac"         -> "audio/aac";
            case "pdf"         -> "application/pdf";
            case "doc"         -> "application/msword";
            case "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default            -> "application/octet-stream";
        };
    }

    // ─────────────────────────────────────────────
    // USED BY: SOAP note attachments/audio — direct server-side putObject
    // ─────────────────────────────────────────────
    public String uploadCompressedFile(byte[] data, String folder, String extension) {

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File data must not be null or empty");
        }

        String ext = extension.toLowerCase().trim();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                    "File extension '." + ext + "' is not allowed. Allowed: " + ALLOWED_EXTENSIONS);
        }

        String contentType = resolveContentType(ext);
        String fileKey = folder + "/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(data));

        return fileKey;
    }

    // ─────────────────────────────────────────────
    // Small wrapper so callers outside this class can validate an
    // extension before doing expensive work that would fail later anyway.
    // ─────────────────────────────────────────────
    public boolean isExtensionAllowed(String extension) {
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase().trim());
    }

    // ─────────────────────────────────────────────
    // USED BY: delete flows that need to remove an object from S3
    // once its DB record is deleted.
    // ─────────────────────────────────────────────
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(
                software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build());
    }
    
   
    
 // ─────────────────────────────────────────────
    // NEW: Download file bytes directly from S3 (server-side)
    // Used by PdfShareController to proxy files without
    // ever exposing the underlying S3 URL to the client.
    // ─────────────────────────────────────────────
    public byte[] downloadFile(String fileKey) {

        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("File key must not be null or blank");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            return s3Client.getObject(getObjectRequest,
                    software.amazon.awssdk.core.sync.ResponseTransformer.toBytes())
                    .asByteArray();

        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found in S3 for key: " + fileKey, e);

        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3 for key: " + fileKey, e);
        }
    }
 
    public void configureQrCodeAutoDelete() {

        LifecycleRuleFilter filter = LifecycleRuleFilter.builder()
                .prefix("qr-codes/")
                .build();

        LifecycleExpiration expiration = LifecycleExpiration.builder()
                .days(3)
                .build();

        AbortIncompleteMultipartUpload abortUpload =
                AbortIncompleteMultipartUpload.builder()
                        .daysAfterInitiation(3)
                        .build();

        LifecycleRule rule = LifecycleRule.builder()
                .id("delete-qr-codes-after-3-days")
                .status("Enabled")
                .filter(filter)
                .expiration(expiration)
                .abortIncompleteMultipartUpload(abortUpload)
                .build();

        BucketLifecycleConfiguration lifecycleConfiguration =
                BucketLifecycleConfiguration.builder()
                        .rules(rule)
                        .build();

        PutBucketLifecycleConfigurationRequest request =
                PutBucketLifecycleConfigurationRequest.builder()
                        .bucket(bucketName)
                        .lifecycleConfiguration(lifecycleConfiguration)
                        .build();

        s3Client.putBucketLifecycleConfiguration(request);
    }
}