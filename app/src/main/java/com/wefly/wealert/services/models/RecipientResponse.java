package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipientResponse {
    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("next")
    @Expose
    private String next;

    @SerializedName("results")
    @Expose
    public List<AlertDataRecipient> recipients;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
