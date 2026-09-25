package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.Holding;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HoldingMapper {

    @Select("SELECT holding_id, account_id, instrument_id, quantity, average_price " +
            "FROM holdings WHERE holding_id = #{holdingId}")
    @Results({
            @Result(property = "holdingId", column = "holding_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "averagePrice", column = "average_price")
    })
    Holding findById(Long holdingId);

    @Select("SELECT holding_id, account_id, instrument_id, quantity, average_price FROM holdings")
    @Results({
            @Result(property = "holdingId", column = "holding_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "averagePrice", column = "average_price")
    })
    List<Holding> findAll();

    @Select("SELECT holding_id, account_id, instrument_id, quantity, average_price " +
            "FROM holdings WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "holdingId", column = "holding_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "averagePrice", column = "average_price")
    })
    List<Holding> findByAccountId(Long accountId);

    @Insert("INSERT INTO holdings (account_id, instrument_id, quantity, average_price) " +
            "VALUES (#{accountId}, #{instrumentId}, #{quantity}, #{averagePrice})")
    @Options(useGeneratedKeys = true, keyProperty = "holdingId", keyColumn = "holding_id")
    int insert(Holding holding);

    @Update("UPDATE holdings SET account_id = #{accountId}, instrument_id = #{instrumentId}, " +
            "quantity = #{quantity}, average_price = #{averagePrice} WHERE holding_id = #{holdingId}")
    int update(Holding holding);

    @Delete("DELETE FROM holdings WHERE holding_id = #{holdingId}")
    int delete(Long holdingId);
}
