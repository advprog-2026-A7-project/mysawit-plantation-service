package com.mysawit.plantation.exception;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlantationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePlantationNotFound(
            PlantationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }
    @ExceptionHandler({
        MandorAssignedException.class,
        InvalidGeometryException.class,
        OverlappingPlantationException.class,
        IllegalArgumentException.class,
        IllegalStateException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(
            RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Plantation data violates a database constraint"));
    }
}
