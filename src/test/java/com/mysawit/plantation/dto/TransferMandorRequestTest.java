package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransferMandorRequestTest {
    @Test
    void testGettersAndSetters() {
        TransferMandorRequest request = new TransferMandorRequest();
        assertNull(request.getMandorId());
        assertNull(request.getFromPlantationId());
        assertNull(request.getToPlantationId());
        
        request.setMandorId("mandor-123");
        request.setFromPlantationId(1L);
        request.setToPlantationId(2L);
        
        assertEquals("mandor-123", request.getMandorId());
        assertEquals(1L, request.getFromPlantationId());
        assertEquals(2L, request.getToPlantationId());
    }
}
