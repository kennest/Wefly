package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Alert {
    @Id private long id;
    private String title;
    private String content;
    private int category;
    private String recipientsID;

    public ToMany<Piece> pieces;
    public Alert() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getRecipientsID() {
        return recipientsID;
    }

    public void setRecipientsID(String recipientsID) {
        this.recipientsID = recipientsID;
    }
}
