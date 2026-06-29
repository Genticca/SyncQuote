package com.syncquote.market.service;

import com.syncquote.exchange.support.ExchangeType;
import com.syncquote.global.exception.BusinessException;
import com.syncquote.global.exception.ErrorCode;
import com.syncquote.market.dto.MarketResponseDto;
import com.syncquote.market.entity.MarketInfo;
import com.syncquote.market.mapper.MarketMapper;
import com.syncquote.market.redis.MarketRedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MarketService {

    private final MarketMapper marketMapper;
    private final MarketRedisRepository marketRedisRepository;

    public MarketService(MarketMapper marketMapper, MarketRedisRepository marketRedisRepository) {
        this.marketMapper = marketMapper;
        this.marketRedisRepository = marketRedisRepository;
    }

    public List<MarketResponseDto> findMarkets(String exchange) {
        if (StringUtils.hasText(exchange)) {
            return findByExchange(parseExchange(exchange));
        }

        List<MarketResponseDto> markets = new ArrayList<>();
        Arrays.stream(ExchangeType.values())
                .map(this::findByExchange)
                .forEach(markets::addAll);
        return markets;
    }

    private List<MarketResponseDto> findByExchange(ExchangeType exchange) {
        List<MarketResponseDto> cachedMarkets = findCachedMarkets(exchange);
        if (!cachedMarkets.isEmpty()) {
            return cachedMarkets;
        }

        return marketMapper.findByExchange(exchange.storageCode())
                .stream()
                .map(MarketResponseDto::from)
                .toList();
    }

    private List<MarketResponseDto> findCachedMarkets(ExchangeType exchange) {
        try {
            return marketRedisRepository.findByExchange(exchange)
                    .stream()
                    .map(market -> MarketResponseDto.from(market, exchange))
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private ExchangeType parseExchange(String exchange) {
        try {
            return ExchangeType.from(exchange);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "지원하지 않는 거래소입니다: " + exchange);
        }
    }
}
