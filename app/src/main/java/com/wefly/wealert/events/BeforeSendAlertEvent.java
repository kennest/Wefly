package com.wefly.wealert.events;

public class BeforeSendAlertEvent {
    public final String message;

    public BeforeSendAlertEvent(String message) {
        this.message = message;
    }
}
