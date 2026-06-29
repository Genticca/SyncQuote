package com.syncquote.ticker.stream;

import com.syncquote.common.sse.SseEventPublisher;
import com.syncquote.common.sse.SseEventType;
import com.syncquote.ticker.dto.TickerStreamResponseDto;
import com.syncquote.ticker.redis.TickerStreamRedisRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TickerStreamConsumer {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    private final TickerStreamRedisRepository tickerStreamRedisRepository;
    private final SseEventPublisher sseEventPublisher;

    public TickerStreamConsumer(
            TickerStreamRedisRepository tickerStreamRedisRepository,
            SseEventPublisher sseEventPublisher
    ) {
        this.tickerStreamRedisRepository = tickerStreamRedisRepository;
        this.sseEventPublisher = sseEventPublisher;
    }

    public void onMessage(TickerStreamMessage message) {
        tickerStreamRedisRepository.append(message);
        sseEventPublisher.broadcast(message.exchange(), message.market(), SseEventType.TICKER, toResponseDto(message));
    }

    private TickerStreamResponseDto toResponseDto(TickerStreamMessage m) {
        return new TickerStreamResponseDto(
                m.exchange(), m.market(), m.tradePrice(), m.changeStatus(), m.changeRate().multiply(PERCENT),
                m.signedChangeRate().multiply(PERCENT), m.signedChangePrice(), m.accTradePrice24h(), m.accTradeVolume24h(),
                m.highPrice(), m.lowPrice(), m.eventTimestamp()
        );
    }
}
