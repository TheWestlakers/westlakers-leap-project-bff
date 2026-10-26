package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthorizationService {
    private static final long ADMIN_ROLE_ID = 1L;

    private final UserCredentialsMapper userCredentialsMapper;

    public AdminAuthorizationService(UserCredentialsMapper userCredentialsMapper) {
        this.userCredentialsMapper = userCredentialsMapper;
    }

    public void requireAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        UserCredentials credentials = userCredentialsMapper.findByEmail(authentication.getName());
        if (credentials == null || !credentials.isActive() || !Long.valueOf(ADMIN_ROLE_ID).equals(credentials.getRoleId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }
}
