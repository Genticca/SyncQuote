package com.syncquote.ticker.mapper;

import com.syncquote.ticker.entity.TickerLatest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TickerMapper {

    List<TickerLatest> findTickers(
            @Param("exchange") String exchange,
            @Param("markets") List<String> markets,
            @Param("category") String category,
            @Param("limit") Integer limit
    );

    TickerLatest findTicker(
            @Param("exchange") String exchange,
            @Param("market") String market
    );
}
