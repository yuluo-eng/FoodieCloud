package com.example.springbootblank.user;

import com.example.springbootblank.auth.entity.User;
import com.example.springbootblank.auth.mapper.AuthMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.user.dto.UserProfileResponse;
import com.example.springbootblank.user.dto.UserProfileUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final AuthMapper authMapper;

    public UserProfileServiceImpl(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Override
    public UserProfileResponse getProfile(long userId) {
        User u = authMapper.findUserById(userId);
        if (u == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return new UserProfileResponse(
                u.getId(),
                u.getUsername(),
                u.getNickname(),
                u.getAvatar(),
                u.getPhone(),
                u.getReceiverName(),
                u.getShippingPhone(),
                u.getShippingAddress(),
                u.getShippingLat(),
                u.getShippingLng()
        );
    }

    @Override
    public void updateProfile(long userId, UserProfileUpdateRequest req) {
        User u = authMapper.findUserById(userId);
        if (u == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (req.nickname() != null) {
            String n = req.nickname().trim();
            u.setNickname(n.isEmpty() ? null : n);
        }
        if (req.avatar() != null) {
            String a = req.avatar().trim();
            u.setAvatar(a.isEmpty() ? null : a);
        }
        if (req.receiverName() != null) {
            String v = req.receiverName().trim();
            u.setReceiverName(v.isEmpty() ? null : v);
        }
        if (req.shippingPhone() != null) {
            String v = req.shippingPhone().trim();
            u.setShippingPhone(v.isEmpty() ? null : v);
        }
        if (req.shippingAddress() != null) {
            String v = req.shippingAddress().trim();
            u.setShippingAddress(v.isEmpty() ? null : v);
        }
        boolean latPresent = req.shippingLat() != null;
        boolean lngPresent = req.shippingLng() != null;
        if (latPresent != lngPresent) {
            throw new BusinessException(400, "纬度与经度需成对填写");
        }
        if (latPresent) {
            u.setShippingLat(req.shippingLat());
            u.setShippingLng(req.shippingLng());
        }

        authMapper.updateUserProfile(u);
    }
}
