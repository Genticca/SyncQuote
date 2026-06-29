package com.syncquote.candle.mapper;

import com.syncquote.candle.entity.Candle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CandleMapper {

    List<Candle> findCandles(
            @Param("exchange") String exchange,
            @Param("market") String market,
            @Param("intervalType") String intervalType,
            @Param("to") Long to,
            @Param("limit") Integer limit
    );
}
