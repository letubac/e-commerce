package com.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.storage.provider:local}")
    private String storageProvider;

    @Autowired(required = false)
    private SupabaseStorageService supabaseStorageService;

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store file and return the file URL.
     * - If provider=supabase: uploads to Supabase Storage, returns full https://...
     * CDN URL
     * - Otherwise: saves to local disk, returns relative
     * /images/{category}/{filename} path
     */
    public String storeFile(MultipartFile file, String category) {
        if ("supabase".equalsIgnoreCase(storageProvider) && supabaseStorageService != null) {
            log.debug("Uploading file to Supabase Storage, category={}", category);
            return supabaseStorageService.uploadFile(file, category);
        }
        return storeFileLocally(file, category);
    }

    private String storeFileLocally(MultipartFile file, String category) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Invalid path sequence " + originalFileName);
            }

            // Create directory structure: images/{category}/
            Path imagesPath = this.fileStorageLocation.resolve("images");
            Path categoryPath = imagesPath.resolve(category);
            Files.createDirectories(categoryPath);

            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = categoryPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path matching DB format: /images/{category}/{filename}
            return "/images/" + category + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Store multiple files
     */
    public String[] storeFiles(MultipartFile[] files, String category) {
        String[] filePaths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filePaths[i] = storeFile(files[i], category);
        }
        return filePaths;
    }

    /**
     * Delete file — supports both Supabase URLs (https://...) and local relative
     * paths (/images/...)
     */
    public void deleteFile(String filePath) {
        if (filePath == null)
            return;
        if (filePath.startsWith("https://") || filePath.startsWith("http://")) {
            if (supabaseStorageService != null) {
                supabaseStorageService.deleteFile(filePath);
            }
            return;
        }
        try {
            Path file = this.fileStorageLocation.resolve(filePath.substring(1)).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + filePath, ex);
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
