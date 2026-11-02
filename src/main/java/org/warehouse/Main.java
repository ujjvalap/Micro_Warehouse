package org.warehouse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.warehouse.cli.OrderManagementCLI;

/**
 * Entry point for the Quick-Commerce Micro-Warehouse Inventory & Fulfillment Hub.
 * Starts the Spring context and launches the interactive Order Management CLI.
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    private final OrderManagementCLI cli;

    @Value("${interactive.cli:true}")
    private boolean interactiveCli;

    public Main(OrderManagementCLI cli) {
        this.cli = cli;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        // Only launch the interactive CLI loop if interactive.cli is enabled and not running automated tests
        if (interactiveCli) {
            cli.start();
        }
    }
}
