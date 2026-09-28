package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.OrderStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderStatusMapper {

    @Select("SELECT order_status_id, status_name, description FROM order_status WHERE order_status_id = #{orderStatusId}")
    @Results({
            @Result(property = "orderStatusId", column = "order_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    OrderStatus findById(Long orderStatusId);

    @Select("SELECT order_status_id, status_name, description FROM order_status WHERE status_name = #{statusName}")
    @Results({
            @Result(property = "orderStatusId", column = "order_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    OrderStatus findByStatusName(String statusName);

    @Select("SELECT order_status_id, status_name, description FROM order_status")
    @Results({
            @Result(property = "orderStatusId", column = "order_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    List<OrderStatus> findAll();

    @Insert("INSERT INTO order_status (status_name, description) VALUES (#{statusName}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "orderStatusId", keyColumn = "order_status_id")
    int insert(OrderStatus orderStatus);

    @Update("UPDATE order_status SET status_name = #{statusName}, description = #{description} WHERE order_status_id = #{orderStatusId}")
    int update(OrderStatus orderStatus);

    @Delete("DELETE FROM order_status WHERE order_status_id = #{orderStatusId}")
    int delete(Long orderStatusId);
}
