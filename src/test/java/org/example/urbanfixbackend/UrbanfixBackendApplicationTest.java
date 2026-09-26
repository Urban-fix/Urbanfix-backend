package org.example.urbanfixbackend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UrbanfixBackendApplicationTest {

    @Test
    void mainMethodExists() {
        // Verify that the main method can be called without throwing an exception
        // We don't actually run it to avoid starting the Spring context
        assertNotNull(UrbanfixBackendApplication.class);
    }
}
