package com.westlakers.leap_bff.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WatchlistMapper {

    @Delete("DELETE FROM account_watchlist WHERE account_id = #{accountId}")
    int deleteByAccountId(Long accountId);

    @Insert("INSERT INTO account_watchlist (account_id, ticker) VALUES (#{accountId}, #{ticker})")
    int insert(@Param("accountId") Long accountId, @Param("ticker") String ticker);

    @Select("SELECT ticker FROM account_watchlist WHERE account_id = #{accountId} ORDER BY ticker")
    List<String> findTickersByAccountId(Long accountId);
}