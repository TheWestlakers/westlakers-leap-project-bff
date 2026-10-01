package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT user_id, first_name, last_name, phone_number, tax_id, date_of_birth, create_date, status_id " +
            "FROM users WHERE user_id = #{userId}")
    User findById(Long userId);

    @Select("SELECT user_id, first_name, last_name, phone_number, tax_id, date_of_birth, create_date, status_id FROM users")
    List<User> findAll();

    @Insert("INSERT INTO users (first_name, last_name, phone_number, tax_id, date_of_birth, status_id) " +
            "VALUES (#{firstName}, #{lastName}, #{phoneNumber}, #{taxId}, #{dateOfBirth}, #{statusId})")
    @SelectKey(statement = "SELECT currval('users_user_id_seq')", keyProperty = "userId", before = false, resultType = Long.class)
    int insert(User user);

    @Update("UPDATE users SET first_name = #{firstName}, last_name = #{lastName}, phone_number = #{phoneNumber}, " +
            "tax_id = #{taxId}, date_of_birth = #{dateOfBirth}, status_id = #{statusId} WHERE user_id = #{userId}")
    int update(User user);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int delete(Long userId);
}
