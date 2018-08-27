package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class AlertData {
    @Id
    private long id;
    private String titre;
    private String contenu;
    public ToMany<Recipient> destinataires;
    public ToMany<Piece> pieces;
    private Double latitude;
    private Double longitude;
    private String date_de_creation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getDate_de_creation() {
        return date_de_creation;
    }

    public void setDate_de_creation(String date_de_creation) {
        this.date_de_creation = date_de_creation;
    }
}
