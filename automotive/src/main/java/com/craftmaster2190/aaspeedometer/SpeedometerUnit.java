package com.craftmaster2190.aaspeedometer;

enum SpeedometerUnit {
    MPH("mph"),
    KPH("kph");

    public String getAbbreviation() {
        return abbreviation;
    }

    private final String abbreviation;

    SpeedometerUnit(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
