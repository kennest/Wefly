package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmployeId {
    @SerializedName("id")
    @Expose
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
