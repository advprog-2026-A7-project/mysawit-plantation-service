package com.mysawit.plantation.util;

import com.mysawit.plantation.exception.InvalidGeometryException;
import com.mysawit.plantation.model.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeometryValidator {

    private static final double LATITUDE_MIN = -90.0;
    private static final double LATITUDE_MAX = 90.0;
    private static final double LONGITUDE_MIN = -180.0;
    private static final double LONGITUDE_MAX = 180.0;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public boolean isSquare(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() != 4) {
            return false;
        }

        for (Coordinate c : coordinates) {
            if (!isWithinWorldRange(c)) {
                return false;
            }
        }

        double[] dists = new double[6];
        int k = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                dists[k++] = distanceSquared(coordinates.get(i), coordinates.get(j));
            }
        }

        int distinct = countDistinctWithTolerance(dists, 1e-9);
        if (distinct != 2) {
            return false;
        }
        
        for (double d : dists) {
            if (d < 1e-9) return false;
        }

        return true;
    }

    public Polygon createPolygon(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() != 4) {
             throw new IllegalArgumentException("Coordinate list must have exactly 4 points");
        }

        for (Coordinate c : coordinates) {
            if (!isWithinWorldRange(c)) {
                throw new InvalidGeometryException(
                        "Coordinates must be within latitude [-90, 90] and longitude [-180, 180]");
            }
        }

        org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[5];
        for (int i = 0; i < 4; i++) {
            jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(
                coordinates.get(i).getLongitude(), 
                coordinates.get(i).getLatitude()
            );
        }
        jtsCoords[4] = new org.locationtech.jts.geom.Coordinate(
            coordinates.get(0).getLongitude(), 
            coordinates.get(0).getLatitude()
        );
        
        Polygon polygon = geometryFactory.createPolygon(jtsCoords);
        if (!polygon.isValid()) {
            throw new InvalidGeometryException("Provided coordinates form an invalid geometry (e.g. self-intersecting)");
        }
        return polygon;
    }

    private boolean isWithinWorldRange(Coordinate coordinate) {
        if (coordinate == null || coordinate.getLatitude() == null || coordinate.getLongitude() == null) {
            return false;
        }
        double latitude = coordinate.getLatitude();
        double longitude = coordinate.getLongitude();
        return latitude >= LATITUDE_MIN && latitude <= LATITUDE_MAX
                && longitude >= LONGITUDE_MIN && longitude <= LONGITUDE_MAX;
    }

    private double distanceSquared(Coordinate p1, Coordinate p2) {
        double dLat = p1.getLatitude() - p2.getLatitude();
        double dLon = p1.getLongitude() - p2.getLongitude();
        return dLat * dLat + dLon * dLon;
    }

    private int countDistinctWithTolerance(double[] arr, double tol) {
        int count = 0;
        boolean[] counted = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (!counted[i]) {
                count++;
                for (int j = i + 1; j < arr.length; j++) {
                    if (Math.abs(arr[i] - arr[j]) < tol) {
                        counted[j] = true;
                    }
                }
            }
        }
        return count;
    }
}
