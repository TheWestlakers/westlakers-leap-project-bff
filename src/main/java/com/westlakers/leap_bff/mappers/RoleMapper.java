package com.westlakers.leap_bff.mappers;

import com.westlakers.leap_bff.entities.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT role_id, role_name FROM roles WHERE role_id = #{roleId}")
    @Results({
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "roleName", column = "role_name")
    })
    Role findById(Long roleId);

    @Select("SELECT role_id, role_name FROM roles WHERE role_name = #{roleName}")
    @Results({
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "roleName", column = "role_name")
    })
    Role findByRoleName(String roleName);

    @Select("SELECT role_id, role_name FROM roles")
    @Results({
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "roleName", column = "role_name")
    })
    List<Role> findAll();

    @Insert("INSERT INTO roles (role_name) VALUES (#{roleName})")
    @Options(useGeneratedKeys = true, keyProperty = "roleId", keyColumn = "role_id")
    int insert(Role role);

    @Update("UPDATE roles SET role_name = #{roleName} WHERE role_id = #{roleId}")
    int update(Role role);

    @Delete("DELETE FROM roles WHERE role_id = #{roleId}")
    int delete(Long roleId);
}
