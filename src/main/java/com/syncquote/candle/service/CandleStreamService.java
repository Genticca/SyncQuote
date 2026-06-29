package com.syncquote.candle.service;

import com.syncquote.common.sse.SseEmitterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@Service
public class CandleStreamService {

    private final SseEmitterRegistry registry;

    public CandleStreamService(SseEmitterRegistry registry) {
        this.registry = registry;
    }

    public SseEmitter subscribe(String exchange, String market) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Set<String> marketFilter = market == null ? Set.of() : Set.of(market);
        SseEmitterRegistry.Registration registration = registry.register(emitter, exchange, marketFilter);

        emitter.onCompletion(() -> registry.remove(registration));
        emitter.onTimeout(() -> registry.remove(registration));
        emitter.onError(e -> registry.remove(registration));

        return emitter;
    }
}
