package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.AccountStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountStatusMapper {

    @Select("SELECT account_status_id, status_name, description FROM account_status WHERE account_status_id = #{accountStatusId}")
    @Results({
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    AccountStatus findById(Long accountStatusId);

    @Select("SELECT account_status_id, status_name, description FROM account_status WHERE status_name = #{statusName}")
    @Results({
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    AccountStatus findByStatusName(String statusName);

    @Select("SELECT account_status_id, status_name, description FROM account_status")
    @Results({
            @Result(property = "accountStatusId", column = "account_status_id"),
            @Result(property = "statusName", column = "status_name"),
            @Result(property = "description", column = "description")
    })
    List<AccountStatus> findAll();

    @Insert("INSERT INTO account_status (status_name, description) VALUES (#{statusName}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "accountStatusId", keyColumn = "account_status_id")
    int insert(AccountStatus accountStatus);

    @Update("UPDATE account_status SET status_name = #{statusName}, description = #{description} WHERE account_status_id = #{accountStatusId}")
    int update(AccountStatus accountStatus);

    @Delete("DELETE FROM account_status WHERE account_status_id = #{accountStatusId}")
    int delete(Long accountStatusId);
}
