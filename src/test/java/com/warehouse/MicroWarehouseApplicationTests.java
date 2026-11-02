package com.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "interactive.cli=false"
})
class MicroWarehouseApplicationTests {

    @Test
    void contextLoads() {
    }
}
