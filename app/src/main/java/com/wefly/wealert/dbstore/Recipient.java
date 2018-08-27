package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Recipient {
    @Id
    public long id;
    private int raw_id;
    private String username;
    private String avatar;

    public ToOne<AlertData> alertdata;

    public int getRaw_id() {
        return raw_id;
    }

    public void setRaw_id(int raw_id) {
        this.raw_id = raw_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
