package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AlertData {

    @SerializedName("titre")
    @Expose
    private String titre;

    @SerializedName("contenu")
    @Expose
    private String contenu;

    @SerializedName("destinataires")
    @Expose
    private List<AlertDataRecipient> destinataires;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("lat")
    @Expose
    private Double latitude;

    @SerializedName("long")
    @Expose
    private Double longitude;

    @SerializedName("categorie")
    @Expose
    private AlertDataCategory category;

    @SerializedName("date_de_creation")
    @Expose
    private String date_de_creation;

    @SerializedName("piece_join_alerte")
    @Expose
    public List<AlertDataPiece> alertDataPieces =new ArrayList<>();

    public List<AlertDataPiece> getAlertDataPieces() {
        return alertDataPieces;
    }

    public void setAlertDataPieces(List<AlertDataPiece> alertDataPieces) {
        this.alertDataPieces = alertDataPieces;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate_de_creation() {
        return date_de_creation;
    }

    public void setDate_de_creation(String date_de_creation) {
        this.date_de_creation = date_de_creation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<AlertDataRecipient> getDestinataires() {
        return destinataires;
    }

    public void setDestinataires(List<AlertDataRecipient> destinataires) {
        this.destinataires = destinataires;
    }

    public AlertDataCategory getCategory() {
        return category;
    }

    public void setCategory(AlertDataCategory category) {
        this.category = category;
    }
}
