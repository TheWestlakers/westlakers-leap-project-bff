package com.westlakers.leap_bff.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.westlakers.leap_bff.entities.Instrument;

@Mapper 
public interface InstrumentMapper {

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instruments")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "marketId", column = "market_id"),
        @Result(property = "assetClassId", column = "asset_class_id"),
    })
    List<Instrument> findAll();

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instruments WHERE instrument_id = #{instrumentId}")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "marketId", column = "market_id"),
        @Result(property = "assetClassId", column = "asset_class_id"),
    })
    Instrument findById(Long id);

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instrument WHERE ticker = #{ticker} ")
    @Results({
        @Result(property = "instrumentId", column = "instrument_id"),
        @Result(property = "marketId", column = "market_id"),
        @Result(property = "assetClassId", column = "asset_class_id"),
    })
    Instrument findByTicker(String ticker);
}