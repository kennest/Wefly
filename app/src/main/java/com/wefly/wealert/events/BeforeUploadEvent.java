package com.wefly.wealert.events;

public class BeforeUploadEvent {
    public final String message;

    public BeforeUploadEvent(String message) {
        this.message = message;
    }
}
