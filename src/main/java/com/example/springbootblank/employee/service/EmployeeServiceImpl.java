package com.example.springbootblank.employee.service;

import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.employee.dto.EmployeeCreateRequest;
import com.example.springbootblank.employee.dto.EmployeeEnabledUpdateRequest;
import com.example.springbootblank.employee.dto.EmployeeUpdateRequest;
import com.example.springbootblank.employee.entity.Employee;
import com.example.springbootblank.employee.mapper.EmployeeMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MerchantAuthGuard merchantAuthGuard;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper,
                               BCryptPasswordEncoder passwordEncoder,
                               MerchantAuthGuard merchantAuthGuard) {
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @Override
    public Map<String, Object> listEmployees(String authorization,
                                              int page,
                                              int pageSize,
                                              Long shopId,
                                              String realName,
                                              Integer enabled) {
        ensureManager(authorization);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safePageSize;
        String keyword = StringUtils.hasText(realName) ? realName.trim() : null;

        long total = employeeMapper.countEmployees(shopId, keyword, enabled);
        List<Employee> records = employeeMapper.listEmployees(shopId, keyword, enabled, offset, safePageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("page", safePage);
        data.put("pageSize", safePageSize);
        data.put("total", total);
        data.put("records", records);
        return data;
    }

    @Override
    public Map<String, Object> createEmployee(String authorization, EmployeeCreateRequest req) {
        ensureManager(authorization);
        if (employeeMapper.countByUsername(req.username()) > 0) {
            throw new BusinessException(400, "账号已存在");
        }
        if (StringUtils.hasText(req.phone()) && employeeMapper.countByPhone(req.phone()) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }

        Employee employee = new Employee();
        employee.setUsername(req.username());
        employee.setPassword(passwordEncoder.encode(req.password()));
        employee.setRealName(req.realName());
        employee.setPhone(req.phone());
        employee.setShopId(req.shopId());
        employee.setRoleId(req.roleId());
        employee.setEnabled(req.enabled() == null ? 1 : req.enabled());

        employeeMapper.insertEmployee(employee);
        return Map.of("id", employee.getId());
    }

    @Override
    public void updateEmployee(String authorization, Long id, EmployeeUpdateRequest req) {
        ensureManager(authorization);
        Employee employee = new Employee();
        employee.setRealName(req.realName());
        employee.setPhone(req.phone());
        employee.setShopId(req.shopId());
        employee.setRoleId(req.roleId());
        employee.setEnabled(req.enabled());
        employeeMapper.updateEmployee(id, employee);
    }

    @Override
    public void deleteEmployee(String authorization, Long id) {
        ensureManager(authorization);
        employeeMapper.deleteEmployee(id);
    }

    @Override
    public void updateEmployeeEnabled(String authorization, Long id, EmployeeEnabledUpdateRequest req) {
        ensureManager(authorization);
        employeeMapper.updateEmployeeEnabled(id, req.enabled());
    }

    @Override
    public List<Map<String, Object>> roles(String authorization) {
        ensureManager(authorization);
        return employeeMapper.listRoles();
    }

    private void ensureManager(String authorization) {
        merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER");
    }
}
