package com.ecommerce.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Transactional;

/**
 * CoreReadOnlyTx
 * 
 * @version 01-00
 * @since 01-00
 * @author 
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Transactional(transactionManager = "transactionManagerSql", readOnly = true)
/**
 * author: LeTuBac
 */
public @interface CoreReadOnlyTx {

}
