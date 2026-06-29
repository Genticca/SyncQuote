package com.syncquote.ticker.service;

import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.exchange.support.MarketCodeNormalizer;
import com.syncquote.global.exception.BusinessException;
import com.syncquote.global.exception.ErrorCode;
import com.syncquote.market.mapper.MarketMapper;
import com.syncquote.ticker.dto.TickerRequestDto;
import com.syncquote.ticker.dto.TickerResponseDto;
import com.syncquote.ticker.entity.TickerLatest;
import com.syncquote.ticker.mapper.TickerMapper;
import com.syncquote.ticker.redis.TickerRestMetaRedis;
import com.syncquote.ticker.redis.TickerRestMetaRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TickerService {

    private static final Logger log = LoggerFactory.getLogger(TickerService.class);
    private static final BigDecimal PERCENT_MULTIPLIER = BigDecimal.valueOf(100);

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
                throw new BusinessException(ErrorCode.MARKET_NOT_FOUND, "해당 마켓이 존재하지 않습니다: " + exchange + " " + market);
            }
            throw new BusinessException(ErrorCode.TICKER_NOT_FOUND, "해당 마켓의 현재가가 없습니다: " + exchange + " " + market);
        }
        return toResponseDto(row);
    }

    private TickerResponseDto toResponseDto(TickerLatest row) {
        ExchangeType exchange = ExchangeType.from(row.getExchange());
        String symbol = MarketCodeNormalizer.toSymbol(exchange, row.getMarket());
        TickerRestMetaRedis meta = findTickerRestMeta(exchange, row.getMarket());

        return new TickerResponseDto(
                exchange.apiCode(),
                row.getMarket(),
                symbol,
                row.getKoreanName(),
                row.getEnglishName(),
                row.getMarketWarning(),
                row.getTradePrice(),
                row.getChangeStatus(),
                toPercent(row.getChangeRate()),
                toPercent(row.getSignedChangeRate()),
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

    private TickerRestMetaRedis findTickerRestMeta(ExchangeType exchange, String market) {
        try {
            return tickerRestMetaRedisRepository.find(exchange, market);
        } catch (RuntimeException exception) {
            log.warn("Ticker 52-week metadata Redis lookup failed. exchange={}, market={}", exchange.apiCode(), market, exception);
            return null;
        }
    }

    private BigDecimal toPercent(BigDecimal value) {
        return value == null ? null : value.multiply(PERCENT_MULTIPLIER);
    }
}
