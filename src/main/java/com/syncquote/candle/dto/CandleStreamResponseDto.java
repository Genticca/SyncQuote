package com.syncquote.candle.dto;

import com.syncquote.candle.redis.CandleOneSecondRedis;

import java.math.BigDecimal;

public record CandleStreamResponseDto(
        Long time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {

    public static CandleStreamResponseDto from(CandleOneSecondRedis candle) {
        return new CandleStreamResponseDto(
                candle.time(),
                candle.open(),
                candle.high(),
                candle.low(),
                candle.close(),
                candle.volume()
        );
    }
}
