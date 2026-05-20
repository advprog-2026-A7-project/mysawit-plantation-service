package com.mysawit.plantation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransferSupirRequestTest {

    @Test
    void gettersAndSettersWork() {
        TransferSupirRequest request = new TransferSupirRequest();
        assertNull(request.getSupirId());
        assertNull(request.getFromPlantationId());
        assertNull(request.getToPlantationId());

        request.setSupirId("supir-123");
        request.setFromPlantationId(1L);
        request.setToPlantationId(2L);

        assertEquals("supir-123", request.getSupirId());
        assertEquals(1L, request.getFromPlantationId());
        assertEquals(2L, request.getToPlantationId());
    }
}
