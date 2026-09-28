package com.westlakers.leap_bff.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.westlakers.leap_bff.entities.Instrument;

@Mapper 
public interface InstrumentMapper {

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instruments")
    List<Instrument> findAll();

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instruments WHERE instrument_id = #{id}")
    Instrument findById(@Param("id") Long id);

    @Select("SELECT instrument_id, market_id, asset_class_id, ticker, name FROM instruments WHERE ticker = #{ticker}")
    Instrument findByTicker(String ticker);

    @Update("UPDATE instruments SET market_id = #{instrument.marketId}, asset_class_id = #{instrument.assetClassId}, ticker = #{instrument.ticker}, name = #{instrument.name} WHERE instrument_id = #{instrument.instrumentId}")
    int saveInstrument(Instrument instrument);

    @Delete("DELETE FROM instruments WHERE instrument_id = #{id}")
    int deleteInstrumentById(@Param("id") Long id);

    @Insert("INSERT INTO instruments (market_id, asset_class_id, ticker, name) VALUES (#{instrument.marketId}, #{instrument.assetClassId}, #{instrument.ticker}, #{instrument.name})")
    @Options(useGeneratedKeys = true, keyProperty = "instrument.instrumentId")
    int createInstrument(Instrument instrument);
}