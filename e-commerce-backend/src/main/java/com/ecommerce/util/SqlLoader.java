package com.ecommerce.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SqlLoader {
    
    private final Map<String, String> queryCache = new HashMap<>();
    
    public String loadQuery(String queryPath) {
        if (queryCache.containsKey(queryPath)) {
            return queryCache.get(queryPath);
        }
        
        try {
            ClassPathResource resource = new ClassPathResource(queryPath);
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String query = new String(bdata, StandardCharsets.UTF_8);
            queryCache.put(queryPath, query);
            return query;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL query from: " + queryPath, e);
        }
    }
}