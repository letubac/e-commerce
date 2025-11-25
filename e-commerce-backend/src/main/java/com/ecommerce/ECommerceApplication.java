package com.ecommerce;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
// import org.springframework.kafka.annotation.EnableKafka;  // Disabled Kafka
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ecommerce.repository.ProductRepository;

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
    
    @Bean
    public ApplicationRunner debugRepo(ApplicationContext ctx) {
        return args -> {
            Object proxy = ctx.getBean(ProductRepository.class);

            System.out.println(">>> Proxy Class = " + proxy.getClass());

            Object real = unwrapProxy(proxy);

            System.out.println(">>> REAL Implementation = " + real.getClass());
        };
    }
    
    public static Object unwrapProxy(Object proxy) {
        try {
            Object current = proxy;

            while (Proxy.isProxyClass(current.getClass())) {

                InvocationHandler handler = Proxy.getInvocationHandler(current);

                Field advisedField = handler.getClass().getDeclaredField("advised");
                advisedField.setAccessible(true);
                Object advised = advisedField.get(handler);

                Method getTargetSource = advised.getClass().getMethod("getTargetSource");
                Object targetSource = getTargetSource.invoke(advised);

                Method getTarget = targetSource.getClass().getMethod("getTarget");
                Object target = getTarget.invoke(targetSource);

                System.out.println(">>> Unwrapped Layer = " + target.getClass());

                // move to next layer
                current = target;
            }

            return current;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}