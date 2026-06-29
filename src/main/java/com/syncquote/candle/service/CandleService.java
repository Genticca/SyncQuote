package com.syncquote.candle.service;

import com.syncquote.candle.dto.CandleResponseDto;
import com.syncquote.candle.mapper.CandleMapper;
import com.syncquote.candle.redis.CandleRedisRepository;
import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.global.exception.BusinessException;
import com.syncquote.global.exception.ErrorCode;
import com.syncquote.market.mapper.MarketMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class CandleService {

    private static final String INTERVAL_ONE_SECOND = "1s";
    private static final String INTERVAL_ONE_MINUTE = "1m";
    private static final Set<String> SUPPORTED_INTERVALS = Set.of(INTERVAL_ONE_SECOND, INTERVAL_ONE_MINUTE);
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_LIMIT = 200;

    private final CandleMapper candleMapper;
    private final CandleRedisRepository candleRedisRepository;
    private final MarketMapper marketMapper;

    public CandleService(
            CandleMapper candleMapper,
            CandleRedisRepository candleRedisRepository,
            MarketMapper marketMapper
    ) {
        this.candleMapper = candleMapper;
        this.candleRedisRepository = candleRedisRepository;
        this.marketMapper = marketMapper;
    }

    public CandleResponseDto getCandles(String exchange, String market, String interval, Integer limit, Long to) {
        if (!StringUtils.hasText(exchange) || !StringUtils.hasText(market) || !StringUtils.hasText(interval)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "exchange, market, interval은 필수입니다.");
        }
        if (!SUPPORTED_INTERVALS.contains(interval)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "interval 값이 올바르지 않습니다: " + interval);
        }

        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit < MIN_LIMIT || resolvedLimit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "limit은 1~1000 사이여야 합니다: " + resolvedLimit);
        }

        long resolvedTo = to == null ? Instant.now().getEpochSecond() : to;
        ExchangeType exchangeType = parseExchange(exchange);

        if (!marketMapper.existsMarket(exchangeType.storageCode(), market)) {
            throw new BusinessException(
                    ErrorCode.MARKET_NOT_FOUND,
                    "해당 마켓이 존재하지 않습니다: " + exchangeType.apiCode() + " " + market
            );
        }

        List<CandleResponseDto.Item> items = INTERVAL_ONE_SECOND.equals(interval)
                ? findRedisCandles(exchangeType, market, resolvedTo, resolvedLimit)
                : findStoredCandles(exchangeType, market, interval, resolvedTo, resolvedLimit);

        return CandleResponseDto.from(exchangeType.apiCode(), market, interval, items);
    }

    private List<CandleResponseDto.Item> findRedisCandles(ExchangeType exchange, String market, long to, int limit) {
        return candleRedisRepository.findRecent(exchange, market, to, limit).stream()
                .map(CandleResponseDto.Item::from)
                .toList();
    }

    private List<CandleResponseDto.Item> findStoredCandles(ExchangeType exchange, String market, String interval, long to, int limit) {
        return candleMapper.findCandles(exchange.storageCode(), market, interval, to, limit).stream()
                .map(CandleResponseDto.Item::from)
                .toList();
    }

    private ExchangeType parseExchange(String exchange) {
        try {
            return ExchangeType.from(exchange);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "지원하지 않는 거래소입니다: " + exchange);
        }
    }
}
