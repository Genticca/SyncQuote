package com.syncquote.market.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.market.entity.MarketInfo;

public record MarketRedis(
        String exchange,
        String market,
        @JsonProperty("korean_name") String koreanName,
        @JsonProperty("english_name") String englishName,
        @JsonProperty("market_warning") String marketWarning,
        @JsonProperty("event_warning") boolean eventWarning,
        @JsonProperty("event_caution") boolean eventCaution
) {

    public static MarketRedis from(MarketInfo marketInfo) {
        ExchangeType exchangeType = ExchangeType.from(marketInfo.getExchange());
        return new MarketRedis(
                exchangeType.storageCode(),
                marketInfo.getMarket(),
                marketInfo.getKoreanName(),
                marketInfo.getEnglishName(),
                marketInfo.getMarketWarning(),
                marketInfo.isEventWarning(),
                marketInfo.isEventCaution()
        );
    }
}
