package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.UserStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserStatusMapper {

    @Select("SELECT user_status_id, status_name, description FROM user_status WHERE user_status_id = #{userStatusId}")
    @Results({
            @Result(property = "userStatusId", column = "user_status_id"),
            @Result(property = "statusName", column = "status_name")
    })
    UserStatus findById(Long userStatusId);

    @Select("SELECT user_status_id, status_name, description FROM user_status WHERE status_name = #{statusName}")
    @Results({
            @Result(property = "userStatusId", column = "user_status_id"),
            @Result(property = "statusName", column = "status_name")
    })
    UserStatus findByStatusName(String statusName);

    @Select("SELECT user_status_id, status_name, description FROM user_status")
    @Results({
            @Result(property = "userStatusId", column = "user_status_id"),
            @Result(property = "statusName", column = "status_name")
    })
    List<UserStatus> findAll();

    @Insert("INSERT INTO user_status (status_name, description) VALUES (#{statusName}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "userStatusId", keyColumn = "user_status_id")
    int insert(UserStatus userStatus);

    @Update("UPDATE user_status SET status_name = #{statusName}, description = #{description} WHERE user_status_id = #{userStatusId}")
    int update(UserStatus userStatus);

    @Delete("DELETE FROM user_status WHERE user_status_id = #{userStatusId}")
    int delete(Long userStatusId);
}
