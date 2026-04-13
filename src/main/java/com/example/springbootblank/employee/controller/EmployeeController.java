package com.example.springbootblank.employee.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.employee.dto.EmployeeCreateRequest;
import com.example.springbootblank.employee.dto.EmployeeEnabledUpdateRequest;
import com.example.springbootblank.employee.dto.EmployeeUpdateRequest;
import com.example.springbootblank.employee.service.EmployeeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public ApiResponse<Map<String, Object>> listEmployees(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam Long shopId,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer enabled
    ) {
        return ApiResponse.ok(employeeService.listEmployees(authorization, page, pageSize, shopId, realName, enabled));
    }

    @PostMapping("/employees")
    public ApiResponse<Map<String, Object>> createEmployee(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody EmployeeCreateRequest req
    ) {
        return ApiResponse.ok(employeeService.createEmployee(authorization, req));
    }

    @PutMapping("/employees/{id}")
    public ApiResponse<Void> updateEmployee(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody EmployeeUpdateRequest req
    ) {
        employeeService.updateEmployee(authorization, id, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/employees/{id}")
    public ApiResponse<Void> deleteEmployee(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        employeeService.deleteEmployee(authorization, id);
        return ApiResponse.ok();
    }

    @PatchMapping("/employees/{id}/enabled")
    public ApiResponse<Void> updateEmployeeEnabled(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody EmployeeEnabledUpdateRequest req
    ) {
        employeeService.updateEmployeeEnabled(authorization, id, req);
        return ApiResponse.ok();
    }

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(employeeService.roles(authorization));
    }
}
