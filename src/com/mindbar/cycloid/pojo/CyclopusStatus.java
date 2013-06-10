package com.mindbar.cycloid.pojo;

/**
 * Created with IntelliJ IDEA.
 * User: Andrey Voloshin
 * Date: 10/06/13
 * Time: 14:55
 */
public class CyclopusStatus {
    private float speed = 0.0F;
    private int cadence = 0;
    private long odometer = 0;
    private long distance = 0;

    public CyclopusStatus(int cadence, float speed, long odometer, long distance){
        this.speed = speed;
        this.cadence = cadence;
        this.odometer = odometer;
        this.distance = distance;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCadence() {
        return cadence;
    }

    public long getOdometer() {
        return odometer;
    }

    public long getDistance() {
        return distance;
    }
}
