package com.wefly.wealert.models;

import android.net.Uri;
import java.io.File;
import java.io.Serializable;

public class Piece implements Serializable{
    private String index;
    private String url;
    private  Uri contentUrl;
    private Integer email;
    private Integer alert;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Uri getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(Uri contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getEmail() {
        return email;
    }

    public void setEmail(Integer email) {
        this.email = email;
    }

    public Integer getAlert() {
        return alert;
    }

    public void setAlert(Integer alert) {
        this.alert = alert;
    }

    public File getFile() {
        return new File(this.getUrl());
    }


    public String getExtension(String url) {
        String ext="";
               ext = url.substring(url.lastIndexOf("."));
        return ext;
    }
}
