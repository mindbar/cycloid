package com.mindbar.cycloid.pojo;

/**
 * Created with IntelliJ IDEA.
 * User: Andrey Voloshin
 * Date: 10/06/13
 * Time: 14:55
 */
public class CyclopusStatus {
    private long speed = 0;
    private long speedMax = 0;
    private int cadence = 0;
    private long odometer = 0;
    private long distance = 0;

    public CyclopusStatus(int cadence, long speed, long speedMax, long odometer, long distance){
        this.speed = speed;
        this.speedMax = speedMax;
        this.cadence = cadence;
        this.odometer = odometer;
        this.distance = distance;
    }

    public float getSpeed() {
        return speed / 100.0f;
    }

    public float getMaxSpeed() {
        return speedMax / 100.0f;
    }

    public int getCadence() {
        return cadence;
    }

    public float getOdometer() {
        return odometer / 100 / 100.0f; // cm to km (18294 cm -> 1.82 km)
    }

    public float getTotalDistance() {
        return distance / 100 / 100.0f; // cm to km
    }
}
