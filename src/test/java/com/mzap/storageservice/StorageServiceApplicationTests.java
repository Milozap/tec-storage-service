package com.mzap.storageservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class StorageServiceApplicationTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> StorageServiceApplication.main(new String[]{}));
    }

}
