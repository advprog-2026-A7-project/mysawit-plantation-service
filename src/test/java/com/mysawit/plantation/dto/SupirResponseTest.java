package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupirResponseTest {

    @Test
    void recordExposesIdAndName() {
        SupirResponse response = new SupirResponse("supir-1", "Budi Driver");

        assertEquals("supir-1", response.id());
        assertEquals("Budi Driver", response.name());
    }
}
