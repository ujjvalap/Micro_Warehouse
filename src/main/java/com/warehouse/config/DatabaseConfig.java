package com.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Spring Configuration defining the core database infrastructure beans:
 * - DataSource: Auto-configured by Spring Boot (HikariCP pool)
 * - JdbcTemplate: Configured with optimized fetch size for batch operations
 * - PlatformTransactionManager: Enables @Transactional boundary management for atomic order fulfillment
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // Optimize fetch size for warehouse order and catalog operations
        jdbcTemplate.setFetchSize(100);
        return jdbcTemplate;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
