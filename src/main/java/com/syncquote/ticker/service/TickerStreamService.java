package com.syncquote.ticker.service;

import com.syncquote.common.sse.SseEmitterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;

@Service
public class TickerStreamService {

    private final SseEmitterRegistry registry;

    public TickerStreamService(SseEmitterRegistry registry) {
        this.registry = registry;
    }

    public SseEmitter subscribe(String exchange, List<String> markets) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Set<String> marketFilter = markets == null ? Set.of() : Set.copyOf(markets);
        SseEmitterRegistry.Registration registration = registry.register(emitter, exchange, marketFilter);

        emitter.onCompletion(() -> registry.remove(registration));
        emitter.onTimeout(() -> registry.remove(registration));
        emitter.onError(e -> registry.remove(registration));

        return emitter;
    }
}
