package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.OrderStatus;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderStatusMapper {

    @Select("SELECT order_status_id, status_name, description FROM order_status WHERE order_status_id = #{orderStatusId}")
    @Results({
            @Result(property = "orderStatusId", column = "order_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    OrderStatus findById(Long orderStatusId);
}
