
package com.warehouse2.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MainApplication implements CommandLineRunner {

    private final ConsoleMenu consoleMenu;

    public MainApplication(ConsoleMenu consoleMenu) {
        this.consoleMenu = consoleMenu;
    }

    @Override
    public void run(String... args) {
        consoleMenu.start();
    }
}
