package com.syncquote.market.scheduler;

import com.syncquote.market.service.MarketSyncService;
import org.springframework.stereotype.Component;

@Component
public class MarketSyncScheduler {

    private final MarketSyncService marketSyncService;

    public MarketSyncScheduler(MarketSyncService marketSyncService) {
        this.marketSyncService = marketSyncService;
    }

    public void syncMarkets() {
        marketSyncService.syncAll();
    }
}
