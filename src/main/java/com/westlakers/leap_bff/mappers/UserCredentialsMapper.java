package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.UserCredentials;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserCredentialsMapper {

    @Select("SELECT credential_id, user_id, role_id, email, username, password_hash, is_active, created_at " +
            "FROM user_credentials WHERE credential_id = #{credentialId}")
    @Results({
            @Result(property = "credentialId", column = "credential_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserCredentials findById(Long credentialId);

    @Select("SELECT credential_id, user_id, role_id, email, username, password_hash, is_active, created_at " +
            "FROM user_credentials WHERE user_id = #{userId}")
    @Results({
            @Result(property = "credentialId", column = "credential_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserCredentials findByUserId(Long userId);

    @Select("SELECT credential_id, user_id, role_id, email, username, password_hash, is_active, created_at " +
            "FROM user_credentials WHERE email = #{email}")
    @Results({
            @Result(property = "credentialId", column = "credential_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserCredentials findByEmail(String email);

    @Select("SELECT credential_id, user_id, role_id, email, username, password_hash, is_active, created_at " +
            "FROM user_credentials WHERE username = #{username}")
    @Results({
            @Result(property = "credentialId", column = "credential_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserCredentials findByUsername(String username);

    @Insert("INSERT INTO user_credentials (user_id, role_id, email, username, password_hash, is_active, created_at) " +
            "VALUES (#{userId}, #{roleId}, #{email}, #{username}, #{passwordHash}, #{isActive}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "credentialId", keyColumn = "credential_id")
    int insert(UserCredentials credentials);

    @Update("UPDATE user_credentials SET user_id = #{userId}, role_id = #{roleId}, email = #{email}, " +
            "username = #{username}, password_hash = #{passwordHash}, is_active = #{isActive}, created_at = #{createdAt} " +
            "WHERE credential_id = #{credentialId}")
    int update(UserCredentials credentials);

    @Delete("DELETE FROM user_credentials WHERE credential_id = #{credentialId}")
    int delete(Long credentialId);
}
