package com.ecommerce.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.MBeanExporter;

import com.ecommerce.constant.AppCoreConstant;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
/**
 * author: LeTuBac
 */
public class DatabaseConfig {

	@Autowired
    private Environment env;
	

	@Profile({"dev"})
    @Bean(name="dataSource")
    public DataSource dataSourceForDev() {
        HikariDataSource datasource = new HikariDataSource();
        datasource.setDriverClassName(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_DRIVER_CLASS_NAME));
        datasource.setJdbcUrl(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_URL));
        datasource.setUsername(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_USERNAME));
        datasource.setPassword(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_PASSWORD));
        datasource.setPoolName(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_POOL_NAME));
        datasource.setAutoCommit(Boolean.valueOf(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_AUTO_COMMIT)));
        //boolean isRegisterMbeans = Boolean.valueOf(env.getProperty(AppCoreConstant.SPRING_DATASOURCE_REGISTER_MBEANS));
        //datasource.setRegisterMbeans(isRegisterMbeans);

        String minimumIdle = env.getProperty(AppCoreConstant.SPRING_DATASOURCE_MINIMUM_IDLE);
        if (minimumIdle != null) {
            datasource.setMinimumIdle(Integer.valueOf(minimumIdle));
        }
        String idleTimeout = env.getProperty(AppCoreConstant.SPRING_DATASOURCE_IDLE_TIMEOUT);
        if (idleTimeout != null) {
            datasource.setIdleTimeout(Long.valueOf(idleTimeout));
        }
        String connectionTimeout = env.getProperty(AppCoreConstant.SPRING_DATASOURCE_CONNECTION_TIMEOUT);
        if (connectionTimeout != null) {
            datasource.setConnectionTimeout(Long.valueOf(connectionTimeout));
        }
        String maxLifetile = env.getProperty(AppCoreConstant.SPRING_DATASOURCE_MAX_LIFETIME);
        if (connectionTimeout != null) {
            datasource.setMaxLifetime(Long.valueOf(maxLifetile));
        }
        return datasource;
    }
    
	@Bean
    public MBeanExporter exporter() {
        final MBeanExporter exporter = new MBeanExporter();
        exporter.setAutodetect(true);
        exporter.setExcludedBeans("dataSource");
        return exporter;
    }
}