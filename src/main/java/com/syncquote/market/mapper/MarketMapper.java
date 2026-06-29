package com.syncquote.market.mapper;

import com.syncquote.market.entity.MarketInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketMapper {

    List<MarketInfo> findAll();

    List<MarketInfo> findByExchange(@Param("exchange") String exchange);
}
