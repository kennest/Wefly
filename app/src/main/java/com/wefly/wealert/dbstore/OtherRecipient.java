package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class OtherRecipient {
    @Id
    private long id;
    private int raw_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRaw_id() {
        return raw_id;
    }

    public void setRaw_id(int raw_id) {
        this.raw_id = raw_id;
    }
}
