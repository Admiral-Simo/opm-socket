package com.network.opmsocket.backend.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class UploadController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 1L * 1024 * 1024; // 1 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "application/msword", // .doc
            "application/vnd.ms-excel", // .xls
            "application/vnd.ms-powerpoint", // .ppt
            "application/zip"
    );

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtAuthenticationToken principal
    ) throws IOException {
        // Ensure the user is authenticated by relying on SecurityConfig

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File too large. Max size is 1MB."));
        }

        String contentType = file.getContentType();
        if (!isAllowedType(file, contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "File type not allowed"));
        }

        // create uploads directory if not exists
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalFilename.substring(dot);
        } else {
            // fallback extension based on content type
            extension = extensionForContentType(contentType);
        }

        // Generate a safe unique filename
        String filename = UUID.randomUUID().toString() + extension;

        Path target = UPLOAD_DIR.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        }

        // Return the relative URL that clients can use
        String url = "/uploads/" + filename;

        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    private boolean isAllowedType(MultipartFile file, String contentType) {
        try {
            // quick check: declared content type
            if (contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType)) {
                // For images/pdf/zip/ms-office formats: verify magic bytes
                if (contentType.startsWith("image/")
                        || contentType.equals("application/pdf")
                        || contentType.equals("application/zip")
                        || contentType.equals("application/msword")
                        || contentType.equals("application/vnd.ms-excel")
                        || contentType.equals("application/vnd.ms-powerpoint")
                        || contentType.startsWith("application/vnd.openxmlformats")) {
                    return hasValidMagicBytes(file);
                }

                // For text files, run a heuristic scan to avoid binary content
                if (contentType.equals("text/plain")) {
                    return isLikelyText(file);
                }

                // otherwise accept declared content type
                return true;
            }

            // if content type is missing or not in allowed list, try to scan the bytes
            return hasValidMagicBytes(file);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasValidMagicBytes(MultipartFile file) throws IOException {
        byte[] header = new byte[16];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(header, 0, header.length);
            if (read <= 0) return false;
        }

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
        // GIF: 'G' 'I' 'F'
        if (header[0] == 'G' && header[1] == 'I' && header[2] == 'F') return true;
        // PDF: starts with %PDF-
        if (header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F') return true;
        // WEBP: 'R' 'I' 'F' 'F' ... 'W' 'E' 'B' 'P' at offset 8
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') return true;

        // ZIP (including OOXML like .docx/.xlsx/.pptx): PK 03 04
        if (header[0] == 'P' && header[1] == 'K' && (header[2] == 3 || header[2] == 5 || header[2] == 6) && header[3] == 4) return true;

        // OLE Compound File (older .doc/.xls/.ppt): D0 CF 11 E0 A1 B1 1A E1
        if ((header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF && (header[2] & 0xFF) == 0x11 && (header[3] & 0xFF) == 0xE0) return true;

        // If content type is text/plain, perform a heuristic text check
        String ct = file.getContentType();
        if ("text/plain".equals(ct)) {
            return isLikelyText(file);
        }

        // not recognized
        return false;
    }

    private boolean isLikelyText(MultipartFile file) throws IOException {
        int sample = 512;
        byte[] buf = new byte[sample];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.read(buf, 0, buf.length);
            if (read <= 0) return false;
        }
        int printable = 0;
        for (int i = 0; i < read; i++) {
            int b = buf[i] & 0xFF;
            if (b == 9 || b == 10 || b == 13) { // allow tab/newline/carriage
                printable++;
            } else if (b >= 32 && b <= 126) { // printable ASCII
                printable++;
            }
        }
        double ratio = (double) printable / (double) read;
        return ratio > 0.9; // accept if >90% printable
    }

    private String extensionForContentType(String contentType) {
        if (contentType == null) return "";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            case "text/plain" -> ".txt";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ".xlsx";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> ".pptx";
            case "application/msword" -> ".doc";
            case "application/vnd.ms-excel" -> ".xls";
            case "application/vnd.ms-powerpoint" -> ".ppt";
            default -> "";
        };
    }
}
