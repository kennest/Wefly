package com.wefly.wealert.tracking;

/**
 * Created by root on 11/30/17.
 */

public class Locations {
    private double mlatitude;
    private double mlongitude;
    private String mdate;

    public Locations(String date, double latitude, double longitude){
        mdate = date;
        mlatitude = latitude;
        mlongitude = longitude;
    }
}
