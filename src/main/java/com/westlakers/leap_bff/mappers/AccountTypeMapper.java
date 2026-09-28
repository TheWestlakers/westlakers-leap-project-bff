package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.AccountType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountTypeMapper {

    @Select("SELECT account_type_id, type_name FROM account_types WHERE account_type_id = #{accountTypeId}")
    @Results({
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "typeName", column = "type_name")
    })
    AccountType findById(Long accountTypeId);

    @Select("SELECT account_type_id, type_name FROM account_types WHERE type_name = #{typeName}")
    @Results({
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "typeName", column = "type_name")
    })
    AccountType findByTypeName(String typeName);

    @Select("SELECT account_type_id, type_name FROM account_types")
    @Results({
            @Result(property = "accountTypeId", column = "account_type_id"),
            @Result(property = "typeName", column = "type_name")
    })
    List<AccountType> findAll();

    @Insert("INSERT INTO account_types (type_name) VALUES (#{typeName})")
    @Options(useGeneratedKeys = true, keyProperty = "accountTypeId", keyColumn = "account_type_id")
    int insert(AccountType accountType);

    @Update("UPDATE account_types SET type_name = #{typeName} WHERE account_type_id = #{accountTypeId}")
    int update(AccountType accountType);

    @Delete("DELETE FROM account_types WHERE account_type_id = #{accountTypeId}")
    int delete(Long accountTypeId);
}
