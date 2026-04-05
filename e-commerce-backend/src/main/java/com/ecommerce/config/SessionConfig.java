package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
/**
 * author: LeTuBac
 */
public class SessionConfig {

    @Value("${app.session.timeout:1800000}") // 30 phút
    private int sessionTimeout;

    @Bean
    public CookieHttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        serializer.setCookieName("ECOMMERCE_SESSION");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(.+)$");
        serializer.setSameSite("Lax");
        serializer.setCookieMaxAge(sessionTimeout / 1000); // Đổi từ ms sang giây

        resolver.setCookieSerializer(serializer);
        return resolver;
    }
}