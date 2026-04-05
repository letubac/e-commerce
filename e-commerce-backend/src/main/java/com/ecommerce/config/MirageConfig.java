package com.ecommerce.config;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import jakarta.annotation.PostConstruct;
import vn.com.unit.miragesql.miragesql.bean.BeanDescFactory;
import vn.com.unit.miragesql.miragesql.bean.FieldPropertyExtractor;
import vn.com.unit.miragesql.miragesql.integration.spring.SpringConnectionProvider;
import vn.com.unit.miragesql.miragesql.naming.NameConverter;
import vn.com.unit.miragesql.miragesql.naming.RailsLikeNameConverter;
import vn.com.unit.springframework.data.mirage.repository.config.EnableMirageRepositories;

@Configuration
@EnableMirageRepositories(
        basePackages = "com.ecommerce.repository",
        sqlManagerRef = "sqlManagerPr"
)
/**
 * author: LeTuBac
 */
public class MirageConfig {

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @PostConstruct
    public void initLoggingBridge() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Bean
    public BeanDescFactory beanDescFactory() {
        BeanDescFactory factory = new BeanDescFactory();
        factory.setPropertyExtractor(new FieldPropertyExtractor());
        return factory;
    }

    @Bean("connectionProvider")
    @Primary
    public SpringConnectionProvider connectionProvider() {
        SpringConnectionProvider provider = new SpringConnectionProvider();
        provider.setTransactionManager(transactionManager);
        return provider;
    }

    // Đăng ký NameConverter như bean để có thể inject vào SqlManagerServiceImpl
    @Bean
    public NameConverter nameConverter() {
        return new RailsLikeNameConverter();
    }

    // NOTE: Không khởi tạo SqlManager ở đây nữa — SqlManagerServiceImpl sẽ là bean "sqlManagerPr"
    @Bean
    public ApplicationListener<ApplicationReadyEvent> logSqlManagerBeans(ApplicationContext ctx) {
        return evt -> {
            String[] names = ctx.getBeanNamesForType(vn.com.unit.miragesql.miragesql.SqlManager.class);
            org.slf4j.LoggerFactory.getLogger("Startup").info("SqlManager beans: {}", java.util.Arrays.toString(names));

            // Lấy và log NameConverter bean (an toàn vì context đã sẵn sàng)
            try {
                Object nc = ctx.getBean(NameConverter.class);
                org.slf4j.LoggerFactory.getLogger("Startup").info("NameConverter bean: {}", nc);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger("Startup").warn("NameConverter bean not found or error getting it: {}", e.toString());
            }
        };
    }
}