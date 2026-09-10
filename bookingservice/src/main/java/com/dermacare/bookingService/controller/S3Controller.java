package com.dermacare.bookingService.controller;

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

import com.dermacare.bookingService.service.S3Service;

@RestController
@RequestMapping("/v1")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    private static final long MB             = 1024 * 1024L;
    private static final long MAX_IMAGE_SIZE =   5 * MB;
    private static final long MAX_VIDEO_SIZE = 100 * MB;
    private static final long MAX_AUDIO_SIZE =  20 * MB;
    private static final long MAX_PDF_SIZE   =  10 * MB;

    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "mov", "avi", "mkv", "webm");
    private static final Set<String> AUDIO_EXTS = Set.of("mp3", "wav", "ogg", "m4a", "aac");
    private static final Set<String> DOC_EXTS   = Set.of("pdf", "doc", "docx");

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

    private record FieldConfig(
            String      folder,
            long        maxAllowedSize,
            String      readableLimit,
            Set<String> allowedExtensions,
            Set<String> allowedMimes
    ) {}

    private FieldConfig resolveConfig(String fieldName) {
        return switch (fieldName) {
            case "attachment" -> new FieldConfig(
                    "attachments",
                    MAX_IMAGE_SIZE, "5 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );
            case "consentPdf"   -> new FieldConfig("consent-pdfs",   MAX_PDF_SIZE,   "10 MB", DOC_EXTS,   DOC_MIMES);
            case "partImage"    -> new FieldConfig("part-images",     MAX_IMAGE_SIZE, "5 MB",  IMAGE_EXTS, IMAGE_MIMES);
            case "report"       -> new FieldConfig(
                    "booking-reports",
                    MAX_PDF_SIZE, "10 MB",
                    Stream.concat(IMAGE_EXTS.stream(), DOC_EXTS.stream())
                          .collect(Collectors.toUnmodifiableSet()),
                    Stream.concat(IMAGE_MIMES.stream(), DOC_MIMES.stream())
                          .collect(Collectors.toUnmodifiableSet())
            );
            case "prescription" -> new FieldConfig("prescriptions",  MAX_PDF_SIZE,   "10 MB", DOC_EXTS,   DOC_MIMES);
            case "beforeImage"  -> new FieldConfig("before-images",  MAX_IMAGE_SIZE, "5 MB",  IMAGE_EXTS, IMAGE_MIMES);
            case "afterImage"   -> new FieldConfig("after-images",   MAX_IMAGE_SIZE, "5 MB",  IMAGE_EXTS, IMAGE_MIMES);
            case "beforeVideo"  -> new FieldConfig("before-videos",  MAX_VIDEO_SIZE, "100 MB",VIDEO_EXTS, VIDEO_MIMES);
            case "afterVideo"   -> new FieldConfig("after-videos",   MAX_VIDEO_SIZE, "100 MB",VIDEO_EXTS, VIDEO_MIMES);
            case "voiceRecord"  -> new FieldConfig("voice-records",  MAX_AUDIO_SIZE, "20 MB", AUDIO_EXTS, AUDIO_MIMES);
            default             -> null;
        };
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/s3/upload-url
    //   ?fieldName=partImage
    //   &fileSize=50000
    //   &extension=png
    // ─────────────────────────────────────────────
    @GetMapping("/s3/upload-url")
    public ResponseEntity<?> getUploadUrl(
            @RequestParam String fieldName,
            @RequestParam(required = false, defaultValue = "0") long fileSize,
            @RequestParam(required = false) String extension) {

        FieldConfig config = resolveConfig(fieldName);
        if (config == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Unknown fieldName: '" + fieldName + "'."
                                   + " Allowed values: attachment, consentPdf, partImage,"
                                   + " report, prescription, beforeImage, afterImage,"
                                   + " beforeVideo, afterVideo, voiceRecord"
                    ));
        }

        if (extension != null && !extension.isBlank()) {
            String ext = extension.toLowerCase().trim();
            if (!config.allowedExtensions().contains(ext)) {
                return ResponseEntity
                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
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
            extension = ext;
        } else {
            extension = config.allowedExtensions().iterator().next();
        }

        if (fileSize > 0 && fileSize > config.maxAllowedSize()) {
            String uploadedMB = String.format("%.2f MB", fileSize / (double) MB);
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

        Map<String, String> response =
                s3Service.generatePresignedPutUrl(config.folder(), extension);

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/s3/validate-upload
    //   ?fileKey=part-images/uuid.png
    //   &fieldName=partImage
    // ─────────────────────────────────────────────
    @GetMapping("/s3/validate-upload")
    public ResponseEntity<?> validateUpload(
            @RequestParam String fileKey,
            @RequestParam String fieldName) {

        FieldConfig config = resolveConfig(fieldName);
        if (config == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "valid", false,
                            "error", "Unknown fieldName: '" + fieldName + "'."
                    ));
        }

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
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
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
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
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
    // GET /api/v1/s3/signed-url
    //   ?fileKey=part-images/uuid.png
    // ─────────────────────────────────────────────
    @GetMapping("/s3/signed-url")
    public ResponseEntity<?> getSignedUrl(@RequestParam String fileKey) {
        try {
            String signedUrl = s3Service.generateSignedUrl(fileKey);
            return ResponseEntity.ok(Map.of(
                    "fileKey",   fileKey,
                    "signedUrl", signedUrl,
                    "expiresIn", "7 days"
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}