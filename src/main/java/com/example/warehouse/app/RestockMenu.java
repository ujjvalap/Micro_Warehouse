
package com.example.warehouse.app;

import com.example.warehouse.model.Product;
import com.example.warehouse.service.RestockService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class RestockMenu implements CommandLineRunner {

    private final RestockService restockService;

    public RestockMenu(RestockService restockService) {
        this.restockService = restockService;
    }

    @Override
    public void run(String... args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\n======================================");
            System.out.println("       RESTOCK ALERT MANAGEMENT");
            System.out.println("======================================");

            System.out.println("1. View Low Stock Products");
            System.out.println("2. Replenish Product Stock");
            System.out.println("3. Exit");

            System.out.print("\nEnter your choice: ");

            try {

                int choice = scanner.nextInt();

                switch (choice) {

                    // View Low Stock Products
                    case 1:

                        System.out.print(
                                "Enter minimum stock threshold: "
                        );

                        int threshold = scanner.nextInt();

                        List<Product> products =
                                restockService.getLowStockProducts(threshold);

                        if (products.isEmpty()) {

                            System.out.println(
                                    "\nNo low stock products found."
                            );

                        } else {

                            System.out.println("\n===== LOW STOCK ALERTS =====");

                            System.out.printf(
                                    "%-10s %-20s %-15s %-10s%n",
                                    "SKU ID",
                                    "PRODUCT",
                                    "CATEGORY",
                                    "STOCK"
                            );

                            System.out.println(
                                    "-------------------------------------------------------"
                            );

                            for (Product product : products) {

                                System.out.printf(
                                        "%-10d %-20s %-15s %-10d%n",
                                        product.getSkuId(),
                                        product.getName(),
                                        product.getCategory(),
                                        product.getStockQty()
                                );
                            }

                            System.out.println(
                                    "\nTotal low stock products: " + products.size()
                            );
                        }

                        break;

                    // Replenish Product Stock
                    case 2:

                        System.out.print("Enter SKU ID: ");
                        int skuId = scanner.nextInt();

                        System.out.print(
                                "Enter quantity to replenish: "
                        );

                        int quantity = scanner.nextInt();

                        restockService.replenishStock(skuId, quantity);

                        break;

                    // Exit
                    case 3:

                        System.out.println(
                                "\nExiting Restock Alert Management..."
                        );

                        return;

                    default:

                        System.out.println(
                                "\nInvalid choice! Please select 1, 2 or 3."
                        );
                }

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "\nValidation Error: " + e.getMessage()
                );

            } catch (java.util.InputMismatchException e) {

                System.out.println(
                        "\nInvalid input! Please enter numbers only."
                );

                scanner.nextLine();

            } catch (Exception e) {

                System.out.println(
                        "\nError: " + e.getMessage()
                );
            }
        }
    }
}
