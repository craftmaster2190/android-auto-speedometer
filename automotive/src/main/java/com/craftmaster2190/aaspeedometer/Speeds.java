package com.craftmaster2190.aaspeedometer;

public class Speeds {

    public float getKilometersPerHour() {
        return kilometersPerHour;
    }

    public float getMilesPerHour() {
        return milesPerHour;
    }

    private final float kilometersPerHour;
    private final float milesPerHour;

    public Speeds(float kilometersPerHour, float milesPerHour) {
        this.kilometersPerHour = kilometersPerHour;
        this.milesPerHour = milesPerHour;
    }

    public static Speeds fromMetersPerSecond(float metersPerSecond) {
        float kilometersPerHour = metersPerSecond * 3.6f;
        float milesPerHour = metersPerSecond * 2.23694f;
        return new Speeds(kilometersPerHour, milesPerHour);
    }
}
