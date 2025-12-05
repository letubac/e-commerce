package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "File Upload", description = "File upload and management APIs")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload single file (Admin only)
     */
    @PostMapping("/admin/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload single file", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "products") String category) {

        String filePath = fileStorageService.storeFile(file, category);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", file.getOriginalFilename());
        response.put("filePath", filePath);
        response.put("fileUrl", "/api/v1/files" + filePath);
        response.put("fileSize", String.valueOf(file.getSize()));
        response.put("contentType", file.getContentType());

        return ResponseEntity.ok(ApiResponse.success(response, "File uploaded successfully"));
    }

    /**
     * Upload multiple files (Admin only)
     */
    @PostMapping("/admin/upload/multiple")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload multiple files", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", defaultValue = "products") String category) {

        String[] filePaths = fileStorageService.storeFiles(files, category);

        Map<String, Object> response = new HashMap<>();
        response.put("totalFiles", files.length);
        response.put("filePaths", filePaths);

        return ResponseEntity.ok(ApiResponse.success(response, "Files uploaded successfully"));
    }

    /**
     * Serve static files (Public access for product images)
     */
    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile(@RequestParam(required = false) String download) {
        try {
            // Get the full request path
            String requestPath = ((jakarta.servlet.http.HttpServletRequest) org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()
                    .resolveReference("request")).getRequestURI();

            // Extract file path from request path
            String filePath = requestPath.replace("/api/v1/files", "");

            // Load file as Resource
            Path file = fileStorageService.getFileStorageLocation().resolve(filePath.substring(1)).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Determine content type
                String contentType = determineContentType(file.toString());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));

                // If download parameter is present, set content disposition
                if (download != null) {
                    headers.add(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"");
                }

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete file (Admin only)
     */
    @DeleteMapping("/admin/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete file", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseEntity.ok(ApiResponse.success(null, "File deleted successfully"));
    }

    /**
     * Determine content type from file extension
     */
    private String determineContentType(String fileName) {
        if (fileName.endsWith(".png"))
            return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";
        if (fileName.endsWith(".gif"))
            return "image/gif";
        if (fileName.endsWith(".webp"))
            return "image/webp";
        if (fileName.endsWith(".svg"))
            return "image/svg+xml";
        if (fileName.endsWith(".pdf"))
            return "application/pdf";
        return "application/octet-stream";
    }
}
