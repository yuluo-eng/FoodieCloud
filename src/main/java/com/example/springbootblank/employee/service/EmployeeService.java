package com.example.springbootblank.employee.service;

import com.example.springbootblank.employee.dto.EmployeeCreateRequest;
import com.example.springbootblank.employee.dto.EmployeeEnabledUpdateRequest;
import com.example.springbootblank.employee.dto.EmployeeUpdateRequest;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    Map<String, Object> listEmployees(String authorization,
                                      int page,
                                      int pageSize,
                                      Long shopId,
                                      String realName,
                                      Integer enabled);

    Map<String, Object> createEmployee(String authorization, EmployeeCreateRequest req);

    void updateEmployee(String authorization, Long id, EmployeeUpdateRequest req);

    void deleteEmployee(String authorization, Long id);

    void updateEmployeeEnabled(String authorization, Long id, EmployeeEnabledUpdateRequest req);

    List<Map<String, Object>> roles(String authorization);
}
