CREATE DATABASE IF NOT EXISTS micro_warehouse;

USE micro_warehouse;

CREATE TABLE products (
                          product_id INT PRIMARY KEY AUTO_INCREMENT,
                          product_name VARCHAR(100) NOT NULL,
                          price DECIMAL(10,2) NOT NULL
);

CREATE TABLE orders (
                        order_id INT PRIMARY KEY AUTO_INCREMENT,
                        order_date DATE NOT NULL,
                        status VARCHAR(30) NOT NULL
);

CREATE TABLE order_items (
                             order_item_id INT PRIMARY KEY AUTO_INCREMENT,
                             order_id INT NOT NULL,
                             product_id INT NOT NULL,
                             quantity INT NOT NULL,

                             FOREIGN KEY (order_id) REFERENCES orders(order_id),
                             FOREIGN KEY (product_id) REFERENCES products(product_id)
);