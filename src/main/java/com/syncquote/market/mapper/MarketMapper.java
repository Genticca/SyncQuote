package com.syncquote.market.mapper;

import org.apache.ibatis.annotations.Param;

public interface MarketMapper {

    boolean existsMarket(@Param("exchange") String exchange, @Param("market") String market);
}
