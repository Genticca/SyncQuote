package com.syncquote.common.sse;

public enum SseEventType {
    TICKER("ticker"),
    CANDLE("candle"),
    HEARTBEAT("heartbeat");

    private final String eventName;

    SseEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
