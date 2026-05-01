package com.mysawit.plantation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void constructorAndAccessorsWork() {
        Coordinate coordinate = new Coordinate(1.5, 103.2);

        assertEquals(1.5, coordinate.getLatitude());
        assertEquals(103.2, coordinate.getLongitude());

        coordinate.setLatitude(-2.0);
        coordinate.setLongitude(100.0);

        assertEquals(-2.0, coordinate.getLatitude());
        assertEquals(100.0, coordinate.getLongitude());
    }

    @Test
    void equalsAndHashCodeCoverAllBranches() {
        Coordinate coordinate = new Coordinate(1.0, 2.0);
        Coordinate sameValues = new Coordinate(1.0, 2.0);
        Coordinate differentLatitude = new Coordinate(9.0, 2.0);
        Coordinate differentLongitude = new Coordinate(1.0, 9.0);

        assertEquals(coordinate, coordinate);
        assertNotEquals(coordinate, null);
        assertNotEquals(coordinate, "not-a-coordinate");
        assertEquals(coordinate, sameValues);
        assertEquals(coordinate.hashCode(), sameValues.hashCode());
        assertNotEquals(coordinate, differentLatitude);
        assertNotEquals(coordinate, differentLongitude);
    }
}
