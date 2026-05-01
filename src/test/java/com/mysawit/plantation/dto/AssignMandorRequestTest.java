package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssignMandorRequestTest {
    @Test
    void testGettersAndSetters() {
        AssignMandorRequest request = new AssignMandorRequest();
        assertNull(request.getMandorId());
        
        request.setMandorId("mandor-123");
        assertEquals("mandor-123", request.getMandorId());
    }
}
