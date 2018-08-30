package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class AlertResponse {
    @Id
    public long id;
    private String next;
    public ToMany<AlertData> data;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
