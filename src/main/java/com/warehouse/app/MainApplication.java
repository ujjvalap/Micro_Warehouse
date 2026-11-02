
package com.warehouse.app;

import com.warehouse.config.JdbcConfig;
import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class MainApplication {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(JdbcConfig.class);

        ProductDao productDao = context.getBean(ProductDao.class);

        Scanner scanner = new Scanner(System.in);

        try {
            int choice;

            do {
                System.out.println("\n===== PRODUCT & INVENTORY MANAGEMENT =====");
                System.out.println("1. Add New Product");
                System.out.println("2. View All Products");
                System.out.println("3. Search Product by SKU ID");
                System.out.println("4. Update Product Price and Category");
                System.out.println("5. Replenish Existing Stock");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {

                    case 1 -> {
                        System.out.print("Enter product name: ");
                        String name = scanner.nextLine();

                        System.out.print("Enter category: ");
                        String category = scanner.nextLine();

                        System.out.print("Enter unit price: ");
                        BigDecimal price = new BigDecimal(scanner.nextLine());

                        System.out.print("Enter initial stock quantity: ");
                        int quantity = scanner.nextInt();
                        scanner.nextLine();

                        Product product = new Product(
                                0, name, category, price, quantity
                        );

                        int rows = productDao.addProduct(product);

                        System.out.println(rows == 1
                                ? "Product added successfully!"
                                : "Product could not be added.");
                    }

                    case 2 -> {
                        List<Product> products = productDao.getAllProducts();

                        System.out.println("\n========== PRODUCT LIST ==========");

                        if (products.isEmpty()) {
                            System.out.println("No products available.");
                        } else {
                            for (Product product : products) {
                                System.out.println(product);
                            }
                        }
                    }

                    case 3 -> {
                        System.out.print("Enter SKU ID: ");
                        int skuId = scanner.nextInt();
                        scanner.nextLine();

                        Product product = productDao.getProductById(skuId);

                        if (product != null) {
                            System.out.println("\n========== PRODUCT FOUND ==========");
                            System.out.println(product);
                        } else {
                            System.out.println("Product not found!");
                        }
                    }

                    case 4 -> {
                        System.out.print("Enter SKU ID: ");
                        int skuId = scanner.nextInt();
                        scanner.nextLine();

                        Product existingProduct =
                                productDao.getProductById(skuId);

                        if (existingProduct == null) {
                            System.out.println("Product not found!");
                            break;
                        }

                        System.out.print("Enter new category: ");
                        String category = scanner.nextLine();

                        System.out.print("Enter new unit price: ");
                        BigDecimal price = new BigDecimal(scanner.nextLine());

                        Product updatedProduct = new Product(
                                skuId,
                                existingProduct.getName(),
                                category,
                                price,
                                existingProduct.getStockQty()
                        );

                        int rows = productDao.updateProduct(updatedProduct);

                        System.out.println(rows == 1
                                ? "Product updated successfully!"
                                : "Product update failed!");
                    }

                    case 5 -> {
                        System.out.print("Enter SKU ID: ");
                        int skuId = scanner.nextInt();

                        System.out.print("Enter quantity to replenish: ");
                        int quantity = scanner.nextInt();
                        scanner.nextLine();

                        int rows = productDao.replenishStock(skuId, quantity);

                        System.out.println(rows == 1
                                ? "Stock replenished successfully!"
                                : "Stock replenishment failed!");
                    }

                    case 0 ->
                            System.out.println("Exiting Product Management...");

                    default ->
                            System.out.println("Invalid choice! Try again.");
                }

            } while (choice != 0);

        } finally {
            scanner.close();
            context.close();
        }
    }
}
