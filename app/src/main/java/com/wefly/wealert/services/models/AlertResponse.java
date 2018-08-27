package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AlertResponse extends BaseResponse {
    @SerializedName("next")
    @Expose
    private String next;

    @SerializedName("results")
    @Expose
    public List<AlertData> data = new ArrayList<>();

    public List<AlertData> getData() {
        return data;
    }

    public void setData(List<AlertData> data) {
        this.data = data;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
