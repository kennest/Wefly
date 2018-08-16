package com.wefly.wealert.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by admin on 07/06/2018.
 */

public abstract class Common implements Serializable {
    private static final long serialVersionUID = 10L;
    private int commonId;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextPage;
    private String prevPage;
    private int count;
    private Recipient author;


    public boolean hasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public String getNextPage() {
        if (nextPage == null)
            nextPage = "";
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getPrevPage() {
        if (prevPage == null)
            prevPage = "";
        return prevPage;
    }

    public void setPrevPage(String prevPage) {
        this.prevPage = prevPage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public @NonNull
    Recipient getAuthor() {
        if (author == null)
            author = new Recipient();
        return author;
    }

    public void setAuthor(Recipient author) {
        this.author = author;
    }

    public String getSender() {
        return getAuthor().getUserName();
    }

    public void setSender(@NonNull String name) {
        getAuthor().setUserName(name);
    }

    public int getCommonId() {
        return commonId;
    }

    public void setCommonId(int commonId) {
        this.commonId = commonId;
    }

    public int getHasNextAsInt() {
        int bool = (hasNext) ? 1 : 0;
        return bool;
    }

    public int getHasPreviousAsInt() {
        int bool = (hasPrevious) ? 1 : 0;
        return bool;
    }

    public void setHasNext(int haNext) {
        hasNext = haNext == 1;
    }

    public void setHasPrevious(int haPrev) {
        hasPrevious = haPrev == 1;
    }
}
