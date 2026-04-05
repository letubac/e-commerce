package com.ecommerce.constant;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
/**
 * author: LeTuBac
 */
public class SpringContext implements ApplicationContextAware  {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T getBean(Class<?> beanClass) {
        return (T) context.getBean(beanClass);
    }
}
