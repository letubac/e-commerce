package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
// import org.springframework.kafka.annotation.EnableKafka;  // Disabled Kafka
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@EnableRedisHttpSession
@ComponentScan(basePackages = "com.ecommerce")
@ConfigurationPropertiesScan("com.ecommerce")
// @EnableKafka // Disabled Kafka temporarily
public class ECommerceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ECommerceApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}