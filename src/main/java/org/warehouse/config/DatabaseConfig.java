package org.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Spring Configuration defining core infrastructure beans:
 * - DataSource (provided by Spring Boot HikariCP)
 * - JdbcTemplate for parameterized queries and batch operations
 * - PlatformTransactionManager for atomic multi-step order transactions
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // Optimize fetch size for warehouse batch operations
        jdbcTemplate.setFetchSize(100);
        return jdbcTemplate;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
