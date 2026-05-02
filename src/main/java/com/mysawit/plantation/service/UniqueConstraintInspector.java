package com.mysawit.plantation.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Locale;

@Component
public class UniqueConstraintInspector {

    private static final String CODE_CONSTRAINT_HINT = "code";
    private static final String PG_UNIQUE_VIOLATION_SQLSTATE = "23505";

    public boolean isCodeUniqueViolation(Throwable throwable) {
        ConstraintNameMatch constraintMatch = findHibernateConstraintName(throwable);
        if (constraintMatch.found()) {
            return constraintMatch.matchesCode();
        }

        if (isPostgresUniqueViolation(throwable)) {
            return mentionsCodeInAnyMessage(throwable);
        }

        return inspectMessagesForCodeUnique(throwable);
    }

    private ConstraintNameMatch findHibernateConstraintName(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException hibernateException) {
                String name = hibernateException.getConstraintName();
                if (name != null) {
                    return new ConstraintNameMatch(true, containsCodeHint(name));
                }
                return new ConstraintNameMatch(false, false);
            }
            current = current.getCause();
        }
        return new ConstraintNameMatch(false, false);
    }

    private boolean isPostgresUniqueViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && PG_UNIQUE_VIOLATION_SQLSTATE.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean mentionsCodeInAnyMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && containsCodeHint(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean inspectMessagesForCodeUnique(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                boolean mentionsCode = normalized.contains(CODE_CONSTRAINT_HINT);
                boolean mentionsUniqueConstraint = normalized.contains("unique")
                        || normalized.contains("duplicate")
                        || normalized.contains("constraint");
                if (mentionsCode && mentionsUniqueConstraint) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean containsCodeHint(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(CODE_CONSTRAINT_HINT);
    }

    private record ConstraintNameMatch(boolean found, boolean matchesCode) {
    }
}
