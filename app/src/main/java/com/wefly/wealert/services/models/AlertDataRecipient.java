package com.wefly.wealert.services.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AlertDataRecipient {

    @SerializedName("photo")
    @Expose
    private String photo;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("user")
    @Expose
    public User user;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public class User{
        @SerializedName("username")
        @Expose
        private String username;

        @SerializedName("first_name")
        @Expose
        private String firstname;

        @SerializedName("last_name")
        @Expose
        private String lastname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
    }
}
