package com.westlakers.leap_bff.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.westlakers.leap_bff.entities.Market;

@Mapper
public interface MarketMapper {

    @Select("SELECT market_id, market_name, market_code, timezone, open_time, close_time " +
            "FROM markets WHERE market_id = #{marketId}")
    Market findById(Long marketId);
}