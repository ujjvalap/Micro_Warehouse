USE micro_warehouse;

INSERT INTO products (product_name, price) VALUES
                                               ('Laptop', 50000),
                                               ('Mouse', 500),
                                               ('Keyboard', 1500),
                                               ('Monitor', 10000);

INSERT INTO orders (order_date, status) VALUES
                                            ('2026-10-01', 'FULFILLED'),
                                            ('2026-10-02', 'FULFILLED'),
                                            ('2026-10-03', 'PENDING');

INSERT INTO order_items (order_id, product_id, quantity) VALUES
                                                             (1, 1, 2),
                                                             (1, 2, 5),
                                                             (2, 3, 3),
                                                             (2, 4, 1),
                                                             (3, 1, 1);