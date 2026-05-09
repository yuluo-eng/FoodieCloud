package com.example.springbootblank.employee.mapper;

import com.example.springbootblank.employee.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmployeeMapper {

    long countEmployees(@Param("shopId") Long shopId,
                        @Param("realName") String realName,
                        @Param("enabled") Integer enabled);

    List<Employee> listEmployees(@Param("shopId") Long shopId,
                                 @Param("realName") String realName,
                                 @Param("enabled") Integer enabled,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    Employee findById(@Param("id") Long id);

    int insertEmployee(Employee employee);

    int updateEmployee(@Param("id") Long id, @Param("employee") Employee employee);

    int deleteEmployee(@Param("id") Long id);

    int updateEmployeeEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    List<Map<String, Object>> listRoles();

    int countByUsername(@Param("username") String username);

    int countByPhone(@Param("phone") String phone);
}
