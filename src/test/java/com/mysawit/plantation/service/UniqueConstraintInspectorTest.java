package com.mysawit.plantation.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqueConstraintInspectorTest {

    private final UniqueConstraintInspector inspector = new UniqueConstraintInspector();

    @Test
    void hibernateConstraintNameMentioningCodeReturnsTrue() {
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "could not execute statement",
                new SQLException("duplicate", "23505"),
                "plantations_code_key"
        );

        assertTrue(inspector.isCodeUniqueViolation(hibernateException));
    }

    @Test
    void hibernateConstraintNameForOtherColumnReturnsFalse() {
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "could not execute statement",
                new SQLException("duplicate", "23505"),
                "plantations_owner_id_key"
        );

        assertFalse(inspector.isCodeUniqueViolation(hibernateException));
    }

    @Test
    void hibernateConstraintNameWithCodeWordReturnsTrue() {
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "violation",
                new SQLException("violation", "23505"),
                "uk_plantation_code"
        );

        assertTrue(inspector.isCodeUniqueViolation(hibernateException));
    }

    @Test
    void hibernateConstraintWithNullNameFallsBackToMessageInspection() {
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "duplicate code value violates constraint",
                new SQLException("duplicate code", "23505"),
                null
        );

        assertTrue(inspector.isCodeUniqueViolation(hibernateException));
    }

    @Test
    void hibernateConstraintWithNullNameAndNonCodeMessageFallsThroughToFalse() {
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "duplicate value violates owner_id_key",
                new SQLException("duplicate", "23505"),
                null
        );

        assertFalse(inspector.isCodeUniqueViolation(hibernateException));
    }

    @Test
    void postgresUniqueViolationWithCodeMessageReturnsTrue() {
        SQLException sql = new SQLException("duplicate key value violates plantations_code_key", "23505");
        RuntimeException wrapper = new RuntimeException("wrapper", sql);

        assertTrue(inspector.isCodeUniqueViolation(wrapper));
    }

    @Test
    void postgresUniqueViolationWithoutCodeReferenceReturnsFalse() {
        SQLException sql = new SQLException("duplicate key value violates owner_id_key", "23505");
        RuntimeException wrapper = new RuntimeException("wrapper", sql);

        assertFalse(inspector.isCodeUniqueViolation(wrapper));
    }

    @Test
    void postgresUniqueViolationWithNullMessagesEverywhereReturnsFalse() {
        SQLException sql = new SQLException(null, "23505");
        RuntimeException wrapper = new RuntimeException(null, sql);

        assertFalse(inspector.isCodeUniqueViolation(wrapper));
    }

    @Test
    void otherSqlStateDoesNotShortCircuitToPostgresPath() {
        SQLException sql = new SQLException("some other error", "42000");
        RuntimeException wrapper = new RuntimeException("duplicate", sql);

        assertFalse(inspector.isCodeUniqueViolation(wrapper));
    }

    @Test
    void plainExceptionWithCodeAndConstraintInMessageReturnsTrue() {
        RuntimeException ex = new RuntimeException("code constraint violated");

        assertTrue(inspector.isCodeUniqueViolation(ex));
    }

    @Test
    void plainExceptionWithDuplicateAndCodeInMessageReturnsTrue() {
        RuntimeException ex = new RuntimeException("duplicate code value");

        assertTrue(inspector.isCodeUniqueViolation(ex));
    }

    @Test
    void plainExceptionWithoutCodeReferenceReturnsFalse() {
        RuntimeException ex = new RuntimeException("duplicate key value violates owner_id_key");

        assertFalse(inspector.isCodeUniqueViolation(ex));
    }

    @Test
    void plainExceptionWithNullMessageReturnsFalse() {
        RuntimeException ex = new RuntimeException((String) null);

        assertFalse(inspector.isCodeUniqueViolation(ex));
    }

    @Test
    void traversesNestedCausesUntilCodeMessageFound() {
        RuntimeException root = new RuntimeException("duplicate code value");
        RuntimeException middle = new RuntimeException("middle layer", root);
        RuntimeException top = new RuntimeException("top layer", middle);

        assertTrue(inspector.isCodeUniqueViolation(top));
    }
}
