package com.syncquote.common.sse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseEventPublisher {

    private final SseEmitterRegistry registry;

    public SseEventPublisher(SseEmitterRegistry registry) {
        this.registry = registry;
    }

    public void broadcast(String exchange, String code, SseEventType type, Object payload) {
        for (SseEmitterRegistry.Registration registration : registry.findMatching(exchange, code)) {
            send(registration, type, payload);
        }
    }

    @Scheduled(fixedRate = 15000)
    public void broadcastHeartbeat() {
        String payload = "{\"ts\":" + System.currentTimeMillis() + "}";
        for (SseEmitterRegistry.Registration registration : registry.getAll()) {
            send(registration, SseEventType.HEARTBEAT, payload);
        }
    }

    private void send(SseEmitterRegistry.Registration registration, SseEventType type, Object payload) {
        try {
            registration.emitter().send(SseEmitter.event().name(type.getEventName()).data(payload));
        } catch (IOException e) {
            registry.remove(registration);
        }
    }
}
