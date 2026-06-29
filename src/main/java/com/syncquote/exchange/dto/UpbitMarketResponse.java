package com.syncquote.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpbitMarketResponse(
        String market,
        @JsonProperty("korean_name") String koreanName,
        @JsonProperty("english_name") String englishName,
        @JsonProperty("market_warning") String marketWarning,
        @JsonProperty("market_event") MarketEvent marketEvent
) {

    public boolean eventWarning() {
        return marketEvent != null && marketEvent.warning();
    }

    public boolean eventCaution() {
        if (marketEvent == null || marketEvent.caution() == null) {
            return false;
        }
        if (marketEvent.caution() instanceof Boolean caution) {
            return caution;
        }
        return true;
    }

    public record MarketEvent(
            boolean warning,
            Object caution
    ) {
    }
}
