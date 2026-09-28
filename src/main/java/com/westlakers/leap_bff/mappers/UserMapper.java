package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT user_id, first_name, last_name, phone_number, tax_id, date_of_birth, create_date, status_id " +
            "FROM users WHERE user_id = #{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "taxId", column = "tax_id"),
            @Result(property = "dateOfBirth", column = "date_of_birth"),
            @Result(property = "createDate", column = "create_date"),
            @Result(property = "statusId", column = "status_id")
    })
    User findById(Long userId);

    @Select("SELECT user_id, first_name, last_name, phone_number, tax_id, date_of_birth, create_date, status_id FROM users")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "taxId", column = "tax_id"),
            @Result(property = "dateOfBirth", column = "date_of_birth"),
            @Result(property = "createDate", column = "create_date"),
            @Result(property = "statusId", column = "status_id")
    })
    List<User> findAll();

    @Insert("INSERT INTO users (first_name, last_name, phone_number, tax_id, date_of_birth, status_id) " +
            "VALUES (#{firstName}, #{lastName}, #{phoneNumber}, #{taxId}, #{dateOfBirth}, #{statusId})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insert(User user);

    @Update("UPDATE users SET first_name = #{firstName}, last_name = #{lastName}, phone_number = #{phoneNumber}, " +
            "tax_id = #{taxId}, date_of_birth = #{dateOfBirth}, status_id = #{statusId} WHERE user_id = #{userId}")
    int update(User user);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int delete(Long userId);
}
