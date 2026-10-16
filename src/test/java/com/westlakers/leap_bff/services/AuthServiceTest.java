package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.CurrentUserResponse;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.AccountStatus;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.AccountStatusMapper;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.mappers.UserMapper;
import com.westlakers.leap_bff.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCredentialsMapper userCredentialsMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountStatusMapper accountStatusMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_Success_CreatesUserAndCredentials() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("PlainText123!");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPhoneNumber("5551234567");
        request.setTaxId("123456789");
        request.setDateOfBirth(LocalDate.of(1995, 5, 10));

        when(userCredentialsMapper.findByEmail("newuser@example.com")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(42L);
            return 1;
        });
        when(passwordEncoder.encode("PlainText123!")).thenReturn("hashed-password");
        when(userCredentialsMapper.insert(any(UserCredentials.class))).thenReturn(1);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(42L, response.getUserId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("newuser@example.com", response.getUsername());

        ArgumentCaptor<UserCredentials> credentialsCaptor = ArgumentCaptor.forClass(UserCredentials.class);
        verify(userCredentialsMapper, times(1)).insert(credentialsCaptor.capture());
        assertEquals("hashed-password", credentialsCaptor.getValue().getPasswordHash());
        assertEquals(2L, credentialsCaptor.getValue().getRoleId());
        assertEquals(42L, credentialsCaptor.getValue().getUserId());
    }

    @Test
    void register_DuplicateEmail_ThrowsApiException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("exists@example.com");

        UserCredentials existing = new UserCredentials();
        existing.setCredentialId(99L);

        when(userCredentialsMapper.findByEmail("exists@example.com")).thenReturn(existing);

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(request));
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());

        verify(userMapper, never()).insert(any(User.class));
        verify(userCredentialsMapper, never()).insert(any(UserCredentials.class));
    }

    @Test
    void authenticateAndIssueToken_Success_ReturnsJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("trader@example.com");
        request.setPassword("Password123!");

        UserCredentials credentials = new UserCredentials();
        credentials.setEmail("trader@example.com");
        credentials.setPasswordHash("stored-hash");
        credentials.setActive(true);

        when(userCredentialsMapper.findByEmail("trader@example.com")).thenReturn(credentials);
        when(passwordEncoder.matches("Password123!", "stored-hash")).thenReturn(true);
        when(jwtService.generateToken("trader@example.com")).thenReturn("jwt-token");

        String token = authService.authenticateAndIssueToken(request);

        assertEquals("jwt-token", token);
        verify(jwtService, times(1)).generateToken("trader@example.com");
    }

    @Test
    void authenticateAndIssueToken_InvalidPassword_ThrowsApiException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("trader@example.com");
        request.setPassword("WrongPassword");

        UserCredentials credentials = new UserCredentials();
        credentials.setEmail("trader@example.com");
        credentials.setPasswordHash("stored-hash");
        credentials.setActive(true);

        when(userCredentialsMapper.findByEmail("trader@example.com")).thenReturn(credentials);
        when(passwordEncoder.matches("WrongPassword", "stored-hash")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> authService.authenticateAndIssueToken(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticateAndIssueToken_InactiveUser_ThrowsApiException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("trader@example.com");
        request.setPassword("Password123!");

        UserCredentials credentials = new UserCredentials();
        credentials.setEmail("trader@example.com");
        credentials.setPasswordHash("stored-hash");
        credentials.setActive(false);

        when(userCredentialsMapper.findByEmail(eq("trader@example.com"))).thenReturn(credentials);

        ApiException ex = assertThrows(ApiException.class, () -> authService.authenticateAndIssueToken(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void getCurrentUser_ActiveUser_ReturnsProfileWithAccounts() {
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(7L);
        credentials.setEmail("trader@example.com");
        credentials.setActive(true);

        User user = new User();
        user.setUserId(7L);
        user.setFirstName("Jane");
        user.setLastName("Trader");

        Account account = new Account();
        account.setAccountId(11L);
        account.setUserId(7L);
        account.setAccountStatusId(1L);
        account.setSettledCash(new BigDecimal("2500.00"));

        AccountStatus status = new AccountStatus();
        status.setStatusName("ACTIVE");

        when(userCredentialsMapper.findByEmail("trader@example.com")).thenReturn(credentials);
        when(userMapper.findById(7L)).thenReturn(user);
        when(accountMapper.findByUserId(7L)).thenReturn(List.of(account));
        when(accountStatusMapper.findById(1L)).thenReturn(status);

        CurrentUserResponse response = authService.getCurrentUser("  Trader@Example.com ");

        assertEquals(7L, response.getUserId());
        assertEquals("trader@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Trader", response.getLastName());
        assertEquals(1, response.getAccounts().size());
        assertEquals(11L, response.getAccounts().get(0).getAccountId());
        assertEquals("ACTIVE", response.getAccounts().get(0).getStatusName());
    }

    @Test
    void getCurrentUser_NoAccounts_ReturnsEmptyAccountList() {
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(8L);
        credentials.setEmail("newuser@example.com");
        credentials.setActive(true);

        User user = new User();
        user.setUserId(8L);

        when(userCredentialsMapper.findByEmail("newuser@example.com")).thenReturn(credentials);
        when(userMapper.findById(8L)).thenReturn(user);
        when(accountMapper.findByUserId(8L)).thenReturn(List.of());

        CurrentUserResponse response = authService.getCurrentUser("newuser@example.com");

        assertEquals(8L, response.getUserId());
        assertEquals(0, response.getAccounts().size());
    }

    @Test
    void getCurrentUser_InactiveCredentials_ThrowsUnauthorized() {
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(7L);
        credentials.setActive(false);

        when(userCredentialsMapper.findByEmail("trader@example.com")).thenReturn(credentials);

        ApiException ex = assertThrows(ApiException.class, () -> authService.getCurrentUser("trader@example.com"));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

        verify(userMapper, never()).findById(any());
    }
}
