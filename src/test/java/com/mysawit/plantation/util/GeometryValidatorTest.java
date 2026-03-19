package com.mysawit.plantation.util;

import com.mysawit.plantation.exception.InvalidGeometryException;
import com.mysawit.plantation.model.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeometryValidatorTest {

    private final GeometryValidator geometryValidator = new GeometryValidator();

    @Test
    void isSquareRejectsNullWrongSizeAndDuplicatePoints() {
        assertFalse(geometryValidator.isSquare(null));
        assertFalse(geometryValidator.isSquare(List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 1.0)
        )));
        assertFalse(geometryValidator.isSquare(List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 0.0)
        )));
    }

    @Test
    void createPolygonRejectsWrongSize() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> geometryValidator.createPolygon(List.of(
                        new Coordinate(0.0, 0.0),
                        new Coordinate(0.0, 1.0),
                        new Coordinate(1.0, 1.0)
                ))
        );

        assertEquals("Coordinate list must have exactly 4 points", exception.getMessage());
    }

    @Test
    void createPolygonRejectsSelfIntersectingGeometry() {
        InvalidGeometryException exception = assertThrows(
                InvalidGeometryException.class,
                () -> geometryValidator.createPolygon(List.of(
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 1.0),
                        new Coordinate(0.0, 1.0),
                        new Coordinate(1.0, 0.0)
                ))
        );

        assertTrue(exception.getMessage().contains("invalid geometry"));
    }
}
