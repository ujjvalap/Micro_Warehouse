package com.warehouse;

import com.warehouse.cli.WarehouseConsoleCLI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Mini Hackathon 2026: Micro-Warehouse Inventory & Fulfillment Hub.
 * Main entry point launching Spring context and the interactive console CLI.
 */
@SpringBootApplication
public class MicroWarehouseApplication implements CommandLineRunner {

    private final WarehouseConsoleCLI cli;

    @Value("${interactive.cli:true}")
    private boolean interactiveCli;

    public MicroWarehouseApplication(WarehouseConsoleCLI cli) {
        this.cli = cli;
    }

    public static void main(String[] args) {
        SpringApplication.run(MicroWarehouseApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (interactiveCli) {
            cli.start();
        }
    }
}
