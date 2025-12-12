package com.ecommerce.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.ecommerce.constant.AppApiConstant;

@Configuration
public class I18nConfiguration {

	@Bean(name = "messageSource")
    public ResourceBundleMessageSource getMessageResource()  {
    	ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
        rs.setBasenames("messages/messages");
        rs.setDefaultEncoding(AppApiConstant.UTF_8);
        rs.setUseCodeAsDefaultMessage(true);
        return rs;
    }
    
    @Bean
    public LocaleResolver localeResolvers() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
}
