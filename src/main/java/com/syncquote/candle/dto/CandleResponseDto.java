package com.syncquote.candle.dto;

import com.syncquote.candle.entity.Candle;
import com.syncquote.candle.redis.CandleOneSecondRedis;

import java.math.BigDecimal;
import java.util.List;

public record CandleResponseDto(
        String exchange,
        String market,
        String interval,
        List<Item> candles
) {

    public static CandleResponseDto from(String exchange, String market, String interval, List<Item> candles) {
        return new CandleResponseDto(exchange, market, interval, candles);
    }

    public record Item(
            Long time,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume
    ) {

        public static Item from(Candle candle) {
            return new Item(
                    candle.getCandleTime(),
                    candle.getOpenPrice(),
                    candle.getHighPrice(),
                    candle.getLowPrice(),
                    candle.getClosePrice(),
                    candle.getVolume()
            );
        }

        public static Item from(CandleOneSecondRedis candle) {
            return new Item(
                    candle.time(),
                    candle.open(),
                    candle.high(),
                    candle.low(),
                    candle.close(),
                    candle.volume()
            );
        }
    }
}
