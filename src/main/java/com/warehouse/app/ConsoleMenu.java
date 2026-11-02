
package com.warehouse.app;

import org.springframework.stereotype.Component;

@Component
public class ConsoleMenu {

    private final InputHandler inputHandler;

    // Constructor
    public ConsoleMenu() {
        this.inputHandler = new InputHandler();
    }

    // Start application menu
    public void start() {
        displayMenu();
    }

    // Display main menu
    public void displayMenu() {

        boolean running = true;

        while (running) {

            System.out.println("\n==================================");
            System.out.println("       MICRO WAREHOUSE SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Product Management");
            System.out.println("2. Order Management");
            System.out.println("3. Inventory Management");
            System.out.println("4. Restock Management");
            System.out.println("5. Sales & Reports");
            System.out.println("6. Exit");
            System.out.println("==================================");

            int choice = inputHandler.readChoice(1, 6);

            switch (choice) {

                case 1:
                    System.out.println("Product Management selected.");
                    break;

                case 2:
                    System.out.println("Order Management selected.");
                    break;

                case 3:
                    System.out.println("Inventory Management selected.");
                    break;

                case 4:
                    System.out.println("Restock Management selected.");
                    break;

                case 5:
                    System.out.println("Sales & Reports selected.");
                    break;

                case 6:
                    System.out.println(
                            "Thank you for using Micro Warehouse!"
                    );
                    running = false;
                    break;
            }
        }
    }
}
