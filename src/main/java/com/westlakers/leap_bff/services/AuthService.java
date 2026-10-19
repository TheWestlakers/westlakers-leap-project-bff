package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.CurrentUserResponse;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.AccountStatusMapper;
import com.westlakers.leap_bff.mappers.AccountTypeMapper;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.mappers.UserMapper;
import com.westlakers.leap_bff.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long DEFAULT_USER_STATUS_ID = 1L;
    private static final long DEFAULT_ROLE_ID = 2L;
    private static final long DEFAULT_ACCOUNT_STATUS_ID = 1L; // OPEN

    private final UserMapper userMapper;
    private final UserCredentialsMapper userCredentialsMapper;
    private final AccountMapper accountMapper;
    private final AccountStatusMapper accountStatusMapper;
    private final AccountTypeMapper accountTypeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userCredentialsMapper.findByEmail(normalizedEmail) != null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "An account with that email already exists");
        }

        if (accountTypeMapper.findById(request.getAccountTypeId()) == null) {
            throw new ApiException(ErrorCode.INVALID_ACCOUNT_TYPE, "Account type not found with id: " + request.getAccountTypeId());
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setTaxId(request.getTaxId());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setStatusId(DEFAULT_USER_STATUS_ID);

        int userInsert = userMapper.insert(user);
        if (userInsert == 0 || user.getUserId() == null) {
            throw new ApiException(ErrorCode.CREATION_FAILED, "Failed to create user record");
        }

        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(user.getUserId());
        credentials.setRoleId(DEFAULT_ROLE_ID);
        credentials.setEmail(normalizedEmail);
        credentials.setUsername(normalizedEmail);
        credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credentials.setActive(true);
        credentials.setCreatedAt(LocalDateTime.now());

        int credentialInsert = userCredentialsMapper.insert(credentials);
        if (credentialInsert == 0) {
            throw new ApiException(ErrorCode.CREATION_FAILED, "Failed to create user credentials");
        }

        // Open the user's first trading account in the same transaction so registration is all-or-nothing
        Account account = new Account();
        account.setUserId(user.getUserId());
        account.setAccountTypeId(request.getAccountTypeId());
        account.setAccountStatusId(DEFAULT_ACCOUNT_STATUS_ID);
        account.setSettledCash(request.getSettledCash());

        int accountInsert = accountMapper.insert(account);
        if (accountInsert == 0) {
            throw new ApiException(ErrorCode.ACCOUNT_CREATION_FAILED);
        }

        return new RegisterResponse(user.getUserId(), normalizedEmail, normalizedEmail);
    }

    @Transactional(readOnly = true)
    public String authenticateAndIssueToken(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        UserCredentials credentials = userCredentialsMapper.findByEmail(normalizedEmail);
        if (credentials == null || !credentials.isActive()) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        return jwtService.generateToken(credentials.getEmail());
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        UserCredentials credentials = userCredentialsMapper.findByEmail(normalizeEmail(email));
        if (credentials == null || !credentials.isActive()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        User user = userMapper.findById(credentials.getUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + credentials.getUserId());
        }

        // A newly registered user may not have an account yet, so return an empty list instead of throwing
        List<AccountDTO> accounts = accountMapper.findByUserId(user.getUserId()).stream()
                .map(account -> AccountDTO.fromEntity(account, accountStatusMapper.findById(account.getAccountStatusId())))
                .toList();

        return new CurrentUserResponse(
                user.getUserId(),
                credentials.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                accounts);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
