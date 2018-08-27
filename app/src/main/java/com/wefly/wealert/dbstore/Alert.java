package com.wefly.wealert.dbstore;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Alert {
    @Id(assignable = true)
    public long id;

    private int raw_id;

    private String title;
    private String content;
    private String category;

    @Backlink
    public ToMany<Piece> pieces;

    public ToMany<OtherRecipient> otherRecipients;

    public ToMany<Recipient> recipients;

    public Alert() {
    }

    public int getRaw_id() {
        return raw_id;
    }

    public void setRaw_id(int raw_id) {
        this.raw_id = raw_id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
