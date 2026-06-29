package com.syncquote.market.service;

import com.syncquote.exchange.client.BinanceClient;
import com.syncquote.exchange.client.UpbitClient;
import com.syncquote.exchange.dto.BinanceMarketResponse;
import com.syncquote.exchange.dto.UpbitMarketResponse;
import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.market.entity.MarketInfo;
import com.syncquote.market.mapper.MarketMapper;
import com.syncquote.market.redis.MarketRedis;
import com.syncquote.market.redis.MarketRedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketSyncService {

    private static final String DEFAULT_MARKET_WARNING = "NONE";

    private final UpbitClient upbitClient;
    private final BinanceClient binanceClient;
    private final MarketMapper marketMapper;
    private final MarketRedisRepository marketRedisRepository;

    public MarketSyncService(UpbitClient upbitClient, BinanceClient binanceClient,
                             MarketMapper marketMapper, MarketRedisRepository marketRedisRepository) {
        this.upbitClient = upbitClient;
        this.binanceClient = binanceClient;
        this.marketMapper = marketMapper;
        this.marketRedisRepository = marketRedisRepository;
    }

    @Transactional
    public void syncAll() {
        syncUpbitMarkets();
        syncBinanceMarkets();
    }

    @Transactional
    public List<MarketInfo> syncUpbitMarkets() {
        List<MarketInfo> markets = upbitClient.fetchMarkets()
                .stream()
                .map(this::toMarketInfo)
                .toList();
        persist(ExchangeType.UPBIT, markets);
        return markets;
    }

    @Transactional
    public List<MarketInfo> syncBinanceMarkets() {
        List<MarketInfo> markets = binanceClient.fetchMarkets()
                .stream()
                .filter(BinanceMarketResponse::isTradingUsdtPair)
                .map(this::toMarketInfo)
                .toList();
        persist(ExchangeType.BINANCE, markets);
        return markets;
    }

    private void persist(ExchangeType exchange, List<MarketInfo> markets) {
        if (markets.isEmpty()) {
            marketRedisRepository.saveAll(exchange, List.of());
            return;
        }
        marketMapper.upsertAll(markets);
        marketRedisRepository.saveAll(exchange, markets.stream().map(MarketRedis::from).toList());
    }

    private MarketInfo toMarketInfo(UpbitMarketResponse response) {
        return new MarketInfo(
                ExchangeType.UPBIT.storageCode(),
                response.market(),
                response.koreanName(),
                response.englishName(),
                response.marketWarning() == null ? DEFAULT_MARKET_WARNING : response.marketWarning(),
                response.eventWarning(),
                response.eventCaution()
        );
    }

    private MarketInfo toMarketInfo(BinanceMarketResponse response) {
        return new MarketInfo(
                ExchangeType.BINANCE.storageCode(),
                response.symbol(),
                null,
                response.baseAsset(),
                DEFAULT_MARKET_WARNING,
                false,
                false
        );
    }
}
