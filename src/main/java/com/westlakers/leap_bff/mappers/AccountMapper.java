package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.Account;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountMapper {

    @Select("SELECT account_id, user_id, account_type_id, account_status_id, created_at, settled_cash " +
            "FROM accounts WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "settledCash", column = "settled_cash")
    })
    Account findById(Long accountId);

    @Select("SELECT account_id, user_id, account_type_id, account_status_id, created_at, settled_cash " +
            "FROM accounts WHERE user_id = #{userId}")
    @Results({
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "settledCash", column = "settled_cash")
    })
    List<Account> findByUserId(Long userId);

    @Select("SELECT account_id, user_id, account_type_id, account_status_id, created_at, settled_cash FROM accounts")
    @Results({
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "settledCash", column = "settled_cash")
    })
    List<Account> findAll();

    @Insert("INSERT INTO accounts (user_id, account_type_id, account_status_id, settled_cash) " +
            "VALUES (#{userId}, #{accountTypeId}, #{accountStatusId}, #{settledCash})")
    @Options(useGeneratedKeys = true, keyProperty = "accountId", keyColumn = "account_id")
    int insert(Account account);

    @Update("UPDATE accounts SET user_id = #{userId}, account_type_id = #{accountTypeId}, " +
            "account_status_id = #{accountStatusId}, settled_cash = #{settledCash} WHERE account_id = #{accountId}")
    int update(Account account);

    @Delete("DELETE FROM accounts WHERE account_id = #{accountId}")
    int delete(Long accountId);
}
