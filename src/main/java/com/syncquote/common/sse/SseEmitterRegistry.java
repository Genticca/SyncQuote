package com.syncquote.common.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    public record Registration(SseEmitter emitter, String exchange, Set<String> markets) {

        boolean matches(String candidateExchange, String candidateMarket) {
            if (exchange != null && !exchange.equalsIgnoreCase(candidateExchange)) {
                return false;
            }
            return markets == null || markets.isEmpty() || markets.contains(candidateMarket);
        }
    }

    private final List<Registration> registrations = new CopyOnWriteArrayList<>();

    public Registration register(SseEmitter emitter, String exchange, Set<String> markets) {
        Registration registration = new Registration(emitter, exchange, markets);
        registrations.add(registration);
        return registration;
    }

    public void remove(Registration registration) {
        registrations.remove(registration);
    }

    public List<Registration> findMatching(String exchange, String market) {
        return registrations.stream().filter(r -> r.matches(exchange, market)).toList();
    }

    public List<Registration> getAll() {
        return registrations;
    }
}
