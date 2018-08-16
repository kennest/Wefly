package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AlertData {

    @SerializedName("titre")
    @Expose
    private String titre;

    @SerializedName("contenu")
    @Expose
    private String contenu;

    @SerializedName("destinataires")
    @Expose
    private ArrayList destinataires;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("date_de_creation")
    @Expose
    private String date_de_creation;

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

    public ArrayList getDestinataires() {
        return destinataires;
    }

    public void setDestinataires(ArrayList destinataires) {
        this.destinataires = destinataires;
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
}
