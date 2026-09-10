package com.clinicadmin.controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PdfShareResponseDTO;
import com.clinicadmin.service.PdfShareService;
import com.clinicadmin.service.S3Service;

@RestController
@RequestMapping("/clinic-admin")
public class S3Controller {

    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private PdfShareService pdfShareService;

    // ─────────────────────────────────────────────
    // File size limits (in bytes)
    // ─────────────────────────────────────────────
    private static final long MB             = 1024 * 1024L;
    private static final long MAX_IMAGE_SIZE =   5 * MB;
    private static final long MAX_VIDEO_SIZE = 100 * MB;
    private static final long MAX_AUDIO_SIZE =  20 * MB;
    private static final long MAX_PDF_SIZE   =  10 * MB;

    // ─────────────────────────────────────────────
    // Allowed extensions per type
    // ─────────────────────────────────────────────
    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "mov", "avi", "mkv", "webm");
    private static final Set<String> AUDIO_EXTS = Set.of("mp3", "wav", "ogg", "m4a", "aac");
    private static final Set<String> DOC_EXTS   = Set.of("pdf", "doc", "docx");
    private static final Set<String> PDF_EXTS = Set.of("pdf");
    private static final Set<String> PDF_MIMES = Set.of("application/pdf");

    // ─────────────────────────────────────────────
    // Allowed MIME types per type
    // ─────────────────────────────────────────────
    private static final Set<String> IMAGE_MIMES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> VIDEO_MIMES = Set.of(
            "video/mp4", "video/quicktime", "video/x-msvideo",
            "video/x-matroska", "video/webm"
    );
    private static final Set<String> AUDIO_MIMES = Set.of(
            "audio/mpeg", "audio/wav", "audio/ogg",
            "audio/mp4",  "audio/aac"
    );
    private static final Set<String> DOC_MIMES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // ─────────────────────────────────────────────
    // Helper record to hold field config
    // ─────────────────────────────────────────────
    private record FieldConfig(
            String      folder,
            long        maxAllowedSize,
            String      readableLimit,
            Set<String> allowedExtensions,
            Set<String> allowedMimes
    ) {}

    // ─────────────────────────────────────────────
    // Resolve config — returns null for unknown field
    // ─────────────────────────────────────────────
    private FieldConfig resolveConfig(String fieldName) {
        return switch (fieldName) {
            case "certificate" -> new FieldConfig(
                    "certificates",
                    MAX_PDF_SIZE,
                    "10 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );
            case "beforeImage"   -> new FieldConfig("before-images",              MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "afterImage"    -> new FieldConfig("after-images",               MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "beforeVideo"   -> new FieldConfig("before-videos",              MAX_VIDEO_SIZE, "100 MB", VIDEO_EXTS, VIDEO_MIMES);
            case "afterVideo"    -> new FieldConfig("after-videos",               MAX_VIDEO_SIZE, "100 MB", VIDEO_EXTS, VIDEO_MIMES);
            case "voiceRecord"   -> new FieldConfig("voice-records",              MAX_AUDIO_SIZE, "20 MB",  AUDIO_EXTS, AUDIO_MIMES);
            case "consentPdf"    -> new FieldConfig("consent-pdfs",               MAX_PDF_SIZE,   "10 MB",  DOC_EXTS,   DOC_MIMES);
            case "patient"       -> new FieldConfig("patients",                   MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "doctorPicture" -> new FieldConfig("doctors",                    MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "doctorSignature"-> new FieldConfig("doctor-signatures",         MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "prescription"  -> new FieldConfig("prescriptions",              MAX_PDF_SIZE,   "10 MB",  DOC_EXTS,   DOC_MIMES);
            case "exercise"      -> new FieldConfig("exercises",                  MAX_VIDEO_SIZE, "100 MB", VIDEO_EXTS, VIDEO_MIMES);
            case "branch"        -> new FieldConfig("branches",                   MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "equipment"     -> new FieldConfig("equipments",                 MAX_IMAGE_SIZE, "5 MB",   IMAGE_EXTS, IMAGE_MIMES);
            case "attachmentFile" -> new FieldConfig(
                    "soap-notes/attachments",
                    MAX_IMAGE_SIZE,
                    "5 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                            .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                            .collect(Collectors.toUnmodifiableSet())
            );

            case "audioNote" -> new FieldConfig(
                    "soap-notes/audio",
                    MAX_AUDIO_SIZE,
                    "20 MB",
                    AUDIO_EXTS,
                    AUDIO_MIMES
            );

            // ── Therapist document fields ──────────────────────────────
            case "therapistProfilePhoto" -> new FieldConfig(
                    "therapist-profile-photos",
                    MAX_IMAGE_SIZE,
                    "5 MB",
                    IMAGE_EXTS,
                    IMAGE_MIMES
            );
            case "therapistLicenseCertificate" -> new FieldConfig(
                    "therapist-license-certificates",
                    MAX_PDF_SIZE,
                    "10 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );
            case "therapistDegreeCertificate" -> new FieldConfig(
                    "therapist-degree-certificates",
                    MAX_PDF_SIZE,
                    "10 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );

            case "report" -> new FieldConfig(
                    "reports",
                    MAX_PDF_SIZE,
                    "10 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );
            case "recoverySupportImage" -> new FieldConfig(
                    "recovery-support-images",
                    MAX_IMAGE_SIZE,
                    "5 MB",
                    IMAGE_EXTS,
                    IMAGE_MIMES
            );
            
            case "qrCode" -> new FieldConfig(
                    "qr-codes",
                    MAX_PDF_SIZE,
                    "10 MB",
                    PDF_EXTS,
                    PDF_MIMES
            );
            
            case "patientPdf" -> new FieldConfig(
                    "patient-pdfs",
                    MAX_PDF_SIZE,
                    "10 MB",
                    DOC_EXTS,
                    DOC_MIMES
            );
            case "whatsappSharePdf" -> new FieldConfig(
                    "whatsapp-shares",
                    MAX_PDF_SIZE,
                    "10 MB",
                    DOC_EXTS,
                    DOC_MIMES
            );
            default -> null;
        };
    }

    // ─────────────────────────────────────────────
    // GET /api/s3/upload-url
    //   ?fieldName=therapistProfilePhoto
    //   &extension=jpg              ← REQUIRED
    //   &fileSize=2048000           ← optional (bytes)
    //
    // Response: { uploadUrl, fileKey, contentType }
    // Frontend MUST use contentType value in the
    // PUT Content-Type header when uploading to S3
    // ─────────────────────────────────────────────
    @GetMapping("/api/s3/upload-url")
    public ResponseEntity<?> getUploadUrl(
            @RequestParam String fieldName,
            @RequestParam(required = false, defaultValue = "0") long fileSize,
            @RequestParam(required = false) String extension) {

        // ── 0. Unknown fieldName → 400 ────────────
        FieldConfig config = resolveConfig(fieldName);
        if (config == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Unknown fieldName: '" + fieldName + "'."
                                   + " Allowed values: certificate, beforeImage, afterImage,"
                                   + " beforeVideo, afterVideo, voiceRecord, consentPdf,"
                                   + " patient, doctorPicture, doctorSignature, prescription,"
                                   + " exercise, branch, report, therapistProfilePhoto,"
                                   + " therapistLicenseCertificate, therapistDegreeCertificate"
                    ));
        }

        // ── 1. Extension is REQUIRED — no random default ──
        if (extension == null || extension.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid",        false,
                            "allowedTypes", config.allowedExtensions(),
                            "error",        "extension param is required."
                                          + " Example: &extension=jpg"
                                          + " Allowed for '" + fieldName + "': "
                                          + config.allowedExtensions()
                    ));
        }

        // ── 2. Extension validation ───────────────
        String ext = extension.toLowerCase().trim();
        if (!config.allowedExtensions().contains(ext)) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)           // 415
                    .body(Map.of(
                            "valid",        false,
                            "uploadedType", ext,
                            "allowedTypes", config.allowedExtensions(),
                            "error", String.format(
                                    "Wrong file type '.%s' for '%s'. Accepted types: %s",
                                    ext, fieldName, config.allowedExtensions()
                            )
                    ));
        }

        // ── 3. File size validation ───────────────
        if (fileSize > 0 && fileSize > config.maxAllowedSize()) {
            String uploadedMB = String.format("%.2f MB", fileSize / (double) MB);
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)                // 413
                    .body(Map.of(
                            "valid",        false,
                            "uploadedSize", uploadedMB,
                            "allowedSize",  config.readableLimit(),
                            "error", String.format(
                                    "File size %s exceeds the maximum allowed size of %s for '%s'.",
                                    uploadedMB, config.readableLimit(), fieldName
                            )
                    ));
        }

        // ── 4. Generate presigned PUT URL ─────────
        // contentType is locked into the signature → content-type appears in X-Amz-SignedHeaders
        Map<String, String> response =
                s3Service.generatePresignedPutUrl(config.folder(), ext);

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // GET /api/s3/validate-upload
    //   ?fileKey=therapist-profile-photos/uuid.jpg
    //   &fieldName=therapistProfilePhoto
    // ─────────────────────────────────────────────
    @GetMapping("/api/s3/validate-upload")
    public ResponseEntity<?> validateUpload(
            @RequestParam String fileKey,
            @RequestParam String fieldName) {

        // ── 0. Unknown fieldName → 400 ────────────
        FieldConfig config = resolveConfig(fieldName);
        if (config == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Unknown fieldName: '" + fieldName + "'."
                    ));
        }

        // ── 1. Extension check from fileKey ───────
        String uploadedExt = fileKey.contains(".")
                ? fileKey.substring(fileKey.lastIndexOf('.') + 1).toLowerCase().trim()
                : "";

        if (uploadedExt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Could not determine file extension from key: " + fileKey
                    ));
        }

        if (!config.allowedExtensions().contains(uploadedExt)) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)           // 415
                    .body(Map.of(
                            "valid",        false,
                            "uploadedType", uploadedExt,
                            "allowedTypes", config.allowedExtensions(),
                            "error", String.format(
                                    "Wrong file uploaded. '.%s' is not allowed for '%s'. Expected types: %s",
                                    uploadedExt, fieldName, config.allowedExtensions()
                            )
                    ));
        }

        // ── 2. Fetch S3 metadata (MIME + size) ────
        Map<String, Object> s3Meta = s3Service.getUploadedFileMeta(fileKey);
        if (s3Meta == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "valid", false,
                            "error", "File not found in S3 for key: " + fileKey
                    ));
        }

        String contentType  = (String) s3Meta.getOrDefault("contentType",   "");
        long   uploadedSize = (long)   s3Meta.getOrDefault("contentLength", 0L);

        // ── 3. MIME type check ────────────────────
        if (contentType == null || contentType.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Could not read content-type from S3 for key: " + fileKey
                    ));
        }

        String mimeOnly = contentType.split(";")[0].trim().toLowerCase();

        if (!config.allowedMimes().contains(mimeOnly)) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)           // 415
                    .body(Map.of(
                            "valid",        false,
                            "uploadedMime", mimeOnly,
                            "allowedMimes", config.allowedMimes(),
                            "error", String.format(
                                    "Wrong file content detected. MIME type '%s' is not allowed for '%s'. Expected: %s",
                                    mimeOnly, fieldName, config.allowedMimes()
                            )
                    ));
        }

        // ── 4. File size check ────────────────────
        if (uploadedSize > config.maxAllowedSize()) {
            String uploadedMB = String.format("%.2f MB", uploadedSize / (double) MB);
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of(
                            "valid",        false,
                            "uploadedSize", uploadedMB,
                            "allowedSize",  config.readableLimit(),
                            "error", String.format(
                                    "File size %s exceeds the maximum allowed size of %s for '%s'.",
                                    uploadedMB, config.readableLimit(), fieldName
                            )
                    ));
        }

        // ── 5. All checks passed ──────────────────
        String uploadedMB = String.format("%.2f MB", uploadedSize / (double) MB);
        return ResponseEntity.ok(Map.of(
                "valid",        true,
                "uploadedType", uploadedExt,
                "uploadedMime", mimeOnly,
                "uploadedSize", uploadedMB,
                "allowedSize",  config.readableLimit(),
                "message", String.format(
                        "File '.%s' (%s, %s) is valid for '%s'.",
                        uploadedExt, mimeOnly, uploadedMB, fieldName
                )
        ));
    }

    // ─────────────────────────────────────────────
    // GET /api/s3/signed-url
    //   ?fileKey=therapist-profile-photos/uuid.jpg
    // ─────────────────────────────────────────────
    @GetMapping("/api/s3/signed-url")
    public ResponseEntity<String> getSignedUrl(
            @RequestParam String fileKey) {

        String signedUrl = s3Service.generateSignedUrl(fileKey);
        return ResponseEntity.ok(signedUrl);
    }
    
    
    @GetMapping("/api/s3/share-url")
    public ResponseEntity<PdfShareResponseDTO> getShareUrl(
            @RequestParam String fileKey) {

        PdfShareResponseDTO response =
                pdfShareService.generateShareUrl(fileKey);

        return ResponseEntity.ok(response);
    }
}