
package com.warehouse.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.ComponentScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan(basePackages = "com.warehouse")
@PropertySource("classpath:application.properties")
public class JdbcConfig {

    @Autowired
    private Environment environment;

    // Create DataSource bean
    @Bean
    public DataSource dataSource() {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName(
                environment.getProperty("db.driver"));

        dataSource.setUrl(
                environment.getProperty("db.url"));

        dataSource.setUsername(
                environment.getProperty("db.username"));

        dataSource.setPassword(
                environment.getProperty("db.password"));

        return dataSource;
    }

    // Create JdbcTemplate bean
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}
