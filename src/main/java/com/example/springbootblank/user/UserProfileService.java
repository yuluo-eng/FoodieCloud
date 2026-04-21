package com.example.springbootblank.user;

import com.example.springbootblank.user.dto.UserProfileResponse;
import com.example.springbootblank.user.dto.UserProfileUpdateRequest;

public interface UserProfileService {

    UserProfileResponse getProfile(long userId);

    void updateProfile(long userId, UserProfileUpdateRequest req);
}
