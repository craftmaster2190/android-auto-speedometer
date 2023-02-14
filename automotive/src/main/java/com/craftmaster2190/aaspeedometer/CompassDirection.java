package com.craftmaster2190.aaspeedometer;

import java.util.Arrays;

public enum CompassDirection {
    NORTH("N", 0),
    NORTHEAST("NE",45),
    EAST("E",90),
    SOUTHEAST("SE",135),
    SOUTH("S",180),
    SOUTHWEST("SW",225),
    WEST("W",270),
    NORTHWEST("NW",315);

    public static CompassDirection fromBearing(float bearing) {
        return Arrays.stream(CompassDirection.values())
                .filter(compassDirection -> bearing >= compassDirection.min && bearing < compassDirection.max)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find CompassDirection fromBearing(" + bearing + ")"));
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    private final String abbreviation;
    private final float min, max;

    CompassDirection(String abbreviation, float centerBearing) {
        this.abbreviation = abbreviation;
        min = centerBearing - 22.5f;
        max = centerBearing + 22.5f;
    }


}
