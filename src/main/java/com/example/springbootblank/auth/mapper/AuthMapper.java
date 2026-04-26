package com.example.springbootblank.auth.mapper;

import com.example.springbootblank.auth.entity.User;
import com.example.springbootblank.employee.entity.Employee;
import com.example.springbootblank.rider.entity.Rider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    int insertUser(User user);

    int countUserByUsername(@Param("username") String username);

    int countUserByPhone(@Param("phone") String phone);

    User findUserByUsername(@Param("username") String username);

    User findUserById(@Param("id") Long id);

    int updateUserProfile(User user);

    Employee findEmployeeByUsernameWithRole(@Param("username") String username);

    Employee findEmployeeByIdWithRole(@Param("id") Long id);

    Rider findRiderByUsername(@Param("username") String username);

    Rider findRiderById(@Param("id") Long id);
}
