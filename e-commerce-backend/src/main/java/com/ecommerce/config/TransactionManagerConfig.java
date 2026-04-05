package com.ecommerce.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
/**
 * author: LeTuBac
 */
public class TransactionManagerConfig {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Bean(name = "transactionManagerSql")
    @Primary
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.setEnforceReadOnly(false);
        return transactionManager;
    }
}
