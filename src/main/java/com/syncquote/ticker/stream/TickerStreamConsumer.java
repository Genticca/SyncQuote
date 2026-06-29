package com.syncquote.ticker.stream;

import com.syncquote.common.sse.SseEmitterRegistry;
import com.syncquote.common.sse.SseEventPublisher;
import com.syncquote.common.sse.SseEventType;
import com.syncquote.ticker.dto.TickerStreamResponseDto;
import com.syncquote.ticker.redis.TickerStreamRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TickerStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(TickerStreamConsumer.class);
    private static final String FIRST_RECORD_ID = "0-0";
    private static final BigDecimal PERCENT_MULTIPLIER = BigDecimal.valueOf(100);

    private final TickerStreamRedisRepository tickerStreamRedisRepository;
    private final SseEventPublisher sseEventPublisher;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final Map<String, String> lastRecordIds = new ConcurrentHashMap<>();

    public TickerStreamConsumer(
            TickerStreamRedisRepository tickerStreamRedisRepository,
            SseEventPublisher sseEventPublisher,
            SseEmitterRegistry sseEmitterRegistry
    ) {
        this.tickerStreamRedisRepository = tickerStreamRedisRepository;
        this.sseEventPublisher = sseEventPublisher;
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishNewTickerEvents() {
        if (sseEmitterRegistry.getAll().isEmpty()) {
            return;
        }

        try {
            for (String key : tickerStreamRedisRepository.findStreamKeys()) {
                String lastRecordId = lastRecordIds.getOrDefault(key, FIRST_RECORD_ID);
                for (TickerStreamRedisRepository.StreamEntry entry : tickerStreamRedisRepository.readAfter(key, lastRecordId)) {
                    lastRecordIds.put(entry.key(), entry.recordId());
                    TickerStreamMessage message = entry.message();
                    sseEventPublisher.broadcast(message.exchange(), message.market(), SseEventType.TICKER, toResponseDto(message));
                }
            }
        } catch (RuntimeException exception) {
            log.warn("Ticker Redis Stream polling failed", exception);
        }
    }

    private TickerStreamResponseDto toResponseDto(TickerStreamMessage message) {
        return new TickerStreamResponseDto(
                message.exchange(),
                message.market(),
                message.tradePrice(),
                message.changeStatus(),
                toPercent(message.changeRate()),
                toPercent(message.signedChangeRate()),
                message.signedChangePrice(),
                message.accTradePrice24h(),
                message.accTradeVolume24h(),
                message.highPrice(),
                message.lowPrice(),
                message.eventTimestamp()
        );
    }

    private BigDecimal toPercent(BigDecimal value) {
        return value == null ? null : value.multiply(PERCENT_MULTIPLIER);
    }
}
