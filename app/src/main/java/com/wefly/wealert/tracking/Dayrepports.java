package com.wefly.wealert.tracking;

import java.util.ArrayList;

/**
 * Created by root on 11/30/17.
 */

public class Dayrepports {
    public String mid;
    public String mdate;
    public ArrayList<Locations> mlocations;
    public Dayrepports(String id, String date, ArrayList<Locations> locations){
        this.mid = id;
        this.mdate = date;
        this.mlocations = locations;
    }

    public void setMid(String id){
        this.mid = id;
    }

    public void setMdate (String date){
        this.mdate = date;
    }

    public void setMlocations (ArrayList<Locations> location){
        this.mlocations = location;
    }

    public String getMdate(){
        return this.mdate;
    }
}
