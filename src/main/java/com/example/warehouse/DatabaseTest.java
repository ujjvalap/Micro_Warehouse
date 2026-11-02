package com.example.warehouse;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTest implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {

        String databaseName = jdbcTemplate.queryForObject(
                "SELECT DATABASE()", String.class
        );

        System.out.println("MySQL Connected Successfully!");
        System.out.println("Database Name: " + databaseName);
    }
}