package com.syncquote.market.entity;

import java.time.LocalDateTime;

public class MarketInfo {

    private String exchange;
    private String market;
    private String koreanName;
    private String englishName;
    private String marketWarning;
    private boolean eventWarning;
    private boolean eventCaution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MarketInfo() {
    }

    public MarketInfo(String exchange, String market, String koreanName, String englishName,
                      String marketWarning, boolean eventWarning, boolean eventCaution) {
        this.exchange = exchange;
        this.market = market;
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.marketWarning = marketWarning;
        this.eventWarning = eventWarning;
        this.eventCaution = eventCaution;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getMarketWarning() {
        return marketWarning;
    }

    public void setMarketWarning(String marketWarning) {
        this.marketWarning = marketWarning;
    }

    public boolean isEventWarning() {
        return eventWarning;
    }

    public void setEventWarning(boolean eventWarning) {
        this.eventWarning = eventWarning;
    }

    public boolean isEventCaution() {
        return eventCaution;
    }

    public void setEventCaution(boolean eventCaution) {
        this.eventCaution = eventCaution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
