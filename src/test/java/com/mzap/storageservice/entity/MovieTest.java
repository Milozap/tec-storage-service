package com.mzap.storageservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovieTest {

    @Test
    @DisplayName("prePersist sets createdAt to current time")
    void prePersistSetsCreatedAt() {
        Movie m = new Movie();
        m.setTitle("Test Movie");
        m.setGenre("Genre");
        m.setReleaseYear(2025);

        assertNull(m.getCreatedAt(), "createdAt should initially be null");
        m.prePersist();
        assertNotNull(m.getCreatedAt(), "createdAt should be set by prePersist");

        LocalDateTime now = LocalDateTime.now();
        assertTrue(
                Duration.between(m.getCreatedAt(), now).abs().getSeconds() < 2,
                "createdAt should be very recent"
        );
    }
}
