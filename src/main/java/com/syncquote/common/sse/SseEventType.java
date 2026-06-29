package com.syncquote.common.sse;

public enum SseEventType {
    TICKER("ticker"),
    HEARTBEAT("heartbeat");

    private final String eventName;

    SseEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
