package com.wefly.wealert.models;

import java.io.Serializable;
/**
 * Created by admin on 13/06/2018.
 */

public class Recipient implements Serializable {
    private static final long serialVersionUID = 10L;


    private int recipientId;


    private int idOnServer;


    private String firstName;


    private String lastName;


    private String userName;

    private String email;

    private String avatarUrl;

    private String tel;
    private String ref;
    private String dateCreate;
    private boolean deleted;
    private int fonction;
    private int adresse;
    private int role;
    private int entreprise;
    private int superieur;
    private int belongTo;
    private int belongId;


    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getIdOnServer() {
        return idOnServer;
    }

    public void setIdOnServer(int idOnServer) {
        this.idOnServer = idOnServer;
    }

    public String getFirstName() {
        if (firstName == null)
            firstName = "";
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        if (lastName == null)
            lastName = "";
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        if (userName == null)
            userName = "";
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        if (email == null)
            email = "";
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        if (tel == null)
            tel = "";
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getRef() {
        if (ref == null)
            ref = "";
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDateCreate() {
        if (dateCreate == null)
            dateCreate = "";
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getFonction() {
        return fonction;
    }

    public void setFonction(int fonction) {
        this.fonction = fonction;
    }

    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(int entreprise) {
        this.entreprise = entreprise;
    }

    public int getSuperieur() {
        return superieur;
    }

    public void setSuperieur(int superieur) {
        this.superieur = superieur;
    }

    public int getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(int belongTo) {
        this.belongTo = belongTo;
    }

    public int getBelongId() {
        return belongId;
    }

    public void setBelongId(int belongId) {
        this.belongId = belongId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
