package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT order_id, account_id, instrument_id, side, order_type, limit_price, quantity, status, placed_at, executed_at " +
            "FROM orders WHERE order_id = #{orderId}")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "side", column = "side"),
            @Result(property = "orderType", column = "order_type"),
            @Result(property = "limitPrice", column = "limit_price"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "statusId", column = "status"),
            @Result(property = "placedAt", column = "placed_at"),
            @Result(property = "executedAt", column = "executed_at")
    })
    Order findById(Long orderId);

    @Select("SELECT order_id, account_id, instrument_id, side, order_type, limit_price, quantity, status, placed_at, executed_at " +
            "FROM orders WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "side", column = "side"),
            @Result(property = "orderType", column = "order_type"),
            @Result(property = "limitPrice", column = "limit_price"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "statusId", column = "status"),
            @Result(property = "placedAt", column = "placed_at"),
            @Result(property = "executedAt", column = "executed_at")
    })
    List<Order> findByAccountId(Long accountId);

    @Select("SELECT order_id, account_id, instrument_id, side, order_type, limit_price, quantity, status, placed_at, executed_at " +
            "FROM orders")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "side", column = "side"),
            @Result(property = "orderType", column = "order_type"),
            @Result(property = "limitPrice", column = "limit_price"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "statusId", column = "status"),
            @Result(property = "placedAt", column = "placed_at"),
            @Result(property = "executedAt", column = "executed_at")
    })
    List<Order> findAll();
}
