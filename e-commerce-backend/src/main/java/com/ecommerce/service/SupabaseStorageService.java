package com.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Supabase Storage integration.
 * Uploads files to Supabase Storage and returns a permanent public CDN URL.
 *
 * Required env vars on Railway:
 * STORAGE_PROVIDER=supabase
 * SUPABASE_URL=https://<project-ref>.supabase.co
 * SUPABASE_STORAGE_BUCKET=images
 * SUPABASE_SERVICE_KEY=<service_role_key> (NOT anon key)
 */
@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${app.storage.supabase.url:}")
    private String supabaseUrl;

    @Value("${app.storage.supabase.bucket:images}")
    private String bucket;

    @Value("${app.storage.supabase.service-key:}")
    private String serviceKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Upload a file to Supabase Storage.
     *
     * @param file     multipart file from client
     * @param category folder inside bucket (e.g. "products", "brands")
     * @return full public CDN URL, e.g.
     *         https://xxx.supabase.co/storage/v1/object/public/images/products/uuid.jpg
     */
    public String uploadFile(MultipartFile file, String category) {
        validateConfig();

        String ext = getExtension(file.getOriginalFilename());
        String objectPath = category + "/" + UUID.randomUUID() + ext;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath;

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true") // overwrite if same path
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
                log.info("Uploaded to Supabase: {}", publicUrl);
                return publicUrl;
            } else {
                throw new RuntimeException(
                        "Supabase upload failed [" + response.statusCode() + "]: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error uploading to Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from Supabase Storage by its public URL.
     */
    public void deleteFile(String publicUrl) {
        validateConfig();

        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!publicUrl.startsWith(prefix)) {
            log.warn("URL is not a Supabase Storage URL, skipping delete: {}", publicUrl);
            return;
        }

        String objectPath = publicUrl.substring(prefix.length());
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .header("Authorization", "Bearer " + serviceKey)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Supabase delete returned [{}]: {}", response.statusCode(), response.body());
            } else {
                log.info("Deleted from Supabase: {}", objectPath);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error deleting from Supabase Storage: {}", publicUrl, e);
        }
    }

    public boolean isConfigured() {
        return supabaseUrl != null && !supabaseUrl.isBlank()
                && serviceKey != null && !serviceKey.isBlank();
    }

    private void validateConfig() {
        if (!isConfigured()) {
            throw new RuntimeException(
                    "Supabase Storage is not configured. Set SUPABASE_URL and SUPABASE_SERVICE_KEY env vars.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.isBlank())
            return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot).toLowerCase();
    }
}
