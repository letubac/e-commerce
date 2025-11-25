package com.ecommerce.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.ecommerce.service.CommonService;
import com.ecommerce.service.impl.CommonServiceImpl;

@Configuration
public class BeanCoreConfiguration {
	
	@Bean
    public CommonService commonService() {
        return new CommonServiceImpl();
    }
    
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
