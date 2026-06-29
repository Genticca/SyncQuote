package com.syncquote.candle.scheduler;

import com.syncquote.candle.dto.CandleStreamResponseDto;
import com.syncquote.candle.redis.CandleOneSecondRedis;
import com.syncquote.candle.redis.CandleRedisRepository;
import com.syncquote.common.sse.SseEmitterRegistry;
import com.syncquote.common.sse.SseEventPublisher;
import com.syncquote.common.sse.SseEventType;
import com.syncquote.exchange.support.ExchangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleRedisPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(CandleRedisPollingScheduler.class);

    private final CandleRedisRepository candleRedisRepository;
    private final SseEventPublisher sseEventPublisher;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final Map<String, Long> lastPublishedTimes = new ConcurrentHashMap<>();

    public CandleRedisPollingScheduler(
            CandleRedisRepository candleRedisRepository,
            SseEventPublisher sseEventPublisher,
            SseEmitterRegistry sseEmitterRegistry
    ) {
        this.candleRedisRepository = candleRedisRepository;
        this.sseEventPublisher = sseEventPublisher;
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishNewCandleEvents() {
        if (sseEmitterRegistry.getAll().isEmpty()) {
            return;
        }

        try {
            for (String key : candleRedisRepository.findStreamKeys()) {
                long lastTime = lastPublishedTimes.getOrDefault(key, Long.MIN_VALUE);
                long maxTime = lastTime;

                for (CandleOneSecondRedis candle : candleRedisRepository.findByKey(key)) {
                    if (candle.time() <= lastTime) {
                        continue;
                    }
                    publish(candle);
                    if (candle.time() > maxTime) {
                        maxTime = candle.time();
                    }
                }

                if (maxTime != lastTime) {
                    lastPublishedTimes.put(key, maxTime);
                }
            }
        } catch (RuntimeException exception) {
            log.warn("Candle Redis List polling failed", exception);
        }
    }

    private void publish(CandleOneSecondRedis candle) {
        ExchangeType exchange = ExchangeType.from(candle.exchange());
        sseEventPublisher.broadcast(
                exchange.apiCode(),
                candle.code(),
                SseEventType.CANDLE,
                CandleStreamResponseDto.from(candle)
        );
    }
}
