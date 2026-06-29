package com.syncquote.market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.exchange.support.MarketCodeNormalizer;
import com.syncquote.market.entity.MarketInfo;
import com.syncquote.market.redis.MarketRedis;

public record MarketResponseDto(
        String exchange,
        String market,
        String symbol,
        @JsonProperty("korean_name") String koreanName,
        @JsonProperty("english_name") String englishName,
        @JsonProperty("market_warning") String marketWarning,
        @JsonProperty("event_warning") boolean eventWarning,
        @JsonProperty("event_caution") boolean eventCaution
) {

    public static MarketResponseDto from(MarketInfo marketInfo) {
        ExchangeType exchangeType = ExchangeType.from(marketInfo.getExchange());
        return new MarketResponseDto(
                exchangeType.apiCode(),
                marketInfo.getMarket(),
                MarketCodeNormalizer.toSymbol(marketInfo.getMarket()),
                marketInfo.getKoreanName(),
                marketInfo.getEnglishName(),
                normalizeWarning(marketInfo.getMarketWarning()),
                marketInfo.isEventWarning(),
                marketInfo.isEventCaution()
        );
    }

    public static MarketResponseDto from(MarketRedis marketRedis, ExchangeType fallbackExchange) {
        ExchangeType exchangeType = marketRedis.exchange() == null || marketRedis.exchange().isBlank()
                ? fallbackExchange
                : ExchangeType.from(marketRedis.exchange());
        return new MarketResponseDto(
                exchangeType.apiCode(),
                marketRedis.market(),
                MarketCodeNormalizer.toSymbol(marketRedis.market()),
                marketRedis.koreanName(),
                marketRedis.englishName(),
                normalizeWarning(marketRedis.marketWarning()),
                marketRedis.eventWarning(),
                marketRedis.eventCaution()
        );
    }

    private static String normalizeWarning(String marketWarning) {
        if (marketWarning == null || marketWarning.isBlank()) {
            return "NONE";
        }
        return marketWarning;
    }
}
