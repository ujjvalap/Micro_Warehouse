
package com.warehouse2.app;

import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;

    // Constructor
    public InputHandler() {
        scanner = new Scanner(System.in);
    }

    // Read text input
    public String readString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    // Read positive integer
    public int readPositiveInt(String message) {

        while (true) {
            System.out.print(message);

            try {
                int value = Integer.parseInt(scanner.nextLine().trim());

                if (value > 0) {
                    return value;
                }

                System.out.println("Please enter a positive number.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }

    // Read menu choice within a given range
    public int readChoice(int min, int max) {

        while (true) {
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                if (choice >= min && choice <= max) {
                    return choice;
                }

                System.out.println(
                        "Invalid choice! Enter between " + min + " and " + max
                );

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
