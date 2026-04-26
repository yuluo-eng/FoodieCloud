package com.example.springbootblank.rider.service;

import java.util.Map;

public interface RiderService {

    Map<String, Object> me(String authorizationHeader);

    void updateWorkStatus(String authorizationHeader, String workStatus);
}
