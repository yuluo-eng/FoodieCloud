package com.example.springbootblank.rider.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.rider.dto.WorkStatusRequest;
import com.example.springbootblank.rider.service.RiderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    private final RiderService riderService;

    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(riderService.me(authorization));
    }

    @PatchMapping("/work-status")
    public ApiResponse<Void> updateWorkStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody WorkStatusRequest req
    ) {
        riderService.updateWorkStatus(authorization, req.workStatus());
        return ApiResponse.ok();
    }
}
