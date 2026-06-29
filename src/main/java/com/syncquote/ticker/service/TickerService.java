package com.syncquote.ticker.service;

import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.exchange.support.MarketCodeNormalizer;
import com.syncquote.global.exception.MarketNotFoundException;
import com.syncquote.global.exception.TickerNotFoundException;
import com.syncquote.market.mapper.MarketMapper;
import com.syncquote.ticker.dto.TickerRequestDto;
import com.syncquote.ticker.dto.TickerResponseDto;
import com.syncquote.ticker.entity.TickerLatest;
import com.syncquote.ticker.mapper.TickerMapper;
import com.syncquote.ticker.redis.TickerRestMetaRedis;
import com.syncquote.ticker.redis.TickerRestMetaRedisRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TickerService {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    private final TickerMapper tickerMapper;
    private final MarketMapper marketMapper;
    private final TickerRestMetaRedisRepository tickerRestMetaRedisRepository;

    public TickerService(
            TickerMapper tickerMapper,
            MarketMapper marketMapper,
            TickerRestMetaRedisRepository tickerRestMetaRedisRepository
    ) {
        this.tickerMapper = tickerMapper;
        this.marketMapper = marketMapper;
        this.tickerRestMetaRedisRepository = tickerRestMetaRedisRepository;
    }

    public List<TickerResponseDto> findTickers(TickerRequestDto request) {
        List<TickerLatest> rows = tickerMapper.findTickers(
                request.getExchange(),
                request.getMarketList(),
                request.getCategory(),
                request.getLimit()
        );
        return rows.stream().map(this::toResponseDto).toList();
    }

    public TickerResponseDto findTicker(String exchange, String market) {
        TickerLatest row = tickerMapper.findTicker(exchange, market);
        if (row == null) {
            if (!marketMapper.existsMarket(exchange, market)) {
                throw new MarketNotFoundException("해당 마켓이 존재하지 않습니다: " + exchange + " " + market);
            }
            throw new TickerNotFoundException("해당 마켓의 현재가가 없습니다: " + exchange + " " + market);
        }
        return toResponseDto(row);
    }

    private TickerResponseDto toResponseDto(TickerLatest row) {
        String symbol = MarketCodeNormalizer.toSymbol(ExchangeType.valueOf(row.getExchange()), row.getMarket());
        TickerRestMetaRedis meta = tickerRestMetaRedisRepository.find(row.getExchange(), row.getMarket());

        return new TickerResponseDto(
                row.getExchange(),
                row.getMarket(),
                symbol,
                row.getKoreanName(),
                row.getEnglishName(),
                row.getMarketWarning(),
                row.getTradePrice(),
                row.getChangeStatus(),
                row.getChangeRate().multiply(PERCENT),
                row.getSignedChangeRate().multiply(PERCENT),
                row.getSignedChangePrice(),
                row.getAccTradePrice24h(),
                row.getAccTradeVolume24h(),
                row.getHighPrice(),
                row.getLowPrice(),
                meta == null ? null : meta.getHighPrice52w(),
                meta == null ? null : meta.getLowPrice52w(),
                row.getEventTimestamp()
        );
    }
}
