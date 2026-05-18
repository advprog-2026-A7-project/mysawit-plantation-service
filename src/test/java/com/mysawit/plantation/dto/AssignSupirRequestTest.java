package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssignSupirRequestTest {

    @Test
    void testGettersAndSetters() {
        AssignSupirRequest request = new AssignSupirRequest();
        assertNull(request.getSupirId());

        request.setSupirId("supir-123");
        assertEquals("supir-123", request.getSupirId());
    }
}
