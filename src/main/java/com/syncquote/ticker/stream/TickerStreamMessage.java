package com.syncquote.ticker.stream;

import com.syncquote.exchange.dto.BinanceTickerResponse;
import com.syncquote.exchange.dto.UpbitTickerResponse;

import java.math.BigDecimal;

public record TickerStreamMessage(
        String exchange,
        String market,
        BigDecimal tradePrice,
        String changeStatus,
        BigDecimal changeRate,
        BigDecimal signedChangeRate,
        BigDecimal signedChangePrice,
        BigDecimal accTradePrice24h,
        BigDecimal accTradeVolume24h,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        Long eventTimestamp
) {

    public static TickerStreamMessage fromUpbit(UpbitTickerResponse r) {
        return new TickerStreamMessage(
                "UPBIT",
                r.code(),
                r.tradePrice(),
                r.change(),
                r.changeRate(),
                r.signedChangeRate(),
                r.signedChangePrice(),
                r.accTradePrice24h(),
                r.accTradeVolume24h(),
                r.highPrice(),
                r.lowPrice(),
                r.tradeTimestamp()
        );
    }

    public static TickerStreamMessage fromBinance(BinanceTickerResponse r) {
        BigDecimal signedChangePrice = r.priceChange();
        BigDecimal signedChangeRate = r.priceChangePercent();
        String changeStatus = signedChangePrice.signum() > 0 ? "RISE" : signedChangePrice.signum() < 0 ? "FALL" : "EVEN";

        return new TickerStreamMessage(
                "BINANCE",
                r.symbol(),
                r.lastPrice(),
                changeStatus,
                signedChangeRate.abs(),
                signedChangeRate,
                signedChangePrice,
                r.quoteVolume(),
                r.baseVolume(),
                r.highPrice(),
                r.lowPrice(),
                r.eventTime()
        );
    }
}
