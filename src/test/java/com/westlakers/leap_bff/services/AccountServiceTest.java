package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.AccountProfileDTO;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.AccountStatus;
import com.westlakers.leap_bff.entities.AccountType;
import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.AccountStatusMapper;
import com.westlakers.leap_bff.mappers.AccountTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountStatusMapper accountStatusMapper;

    @Mock
    private AccountTypeMapper accountTypeMapper;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountDTO testAccountDTO;
    private AccountStatus testStatus;
    private AccountType testType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testStatus = new AccountStatus();
        testStatus.setAccountStatusId(1L);
        testStatus.setStatusName("ACTIVE");

        testType = new AccountType();
        testType.setAccountTypeId(1L);
        testType.setTypeName("BROKERAGE");

        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setUserId(1L);
        testAccount.setAccountTypeId(1L);
        testAccount.setAccountStatusId(1L);
        testAccount.setCurrency("USD");
        testAccount.setSettledCash(new BigDecimal("10000.00"));
        testAccount.setCreatedAt(LocalDateTime.now());

        testAccountDTO = new AccountDTO();
        testAccountDTO.setAccountId(1L);
        testAccountDTO.setUserId(1L);
        testAccountDTO.setAccountTypeId(1L);
        testAccountDTO.setSettledCash(new BigDecimal("10000.00"));
        testAccountDTO.setStatusName("ACTIVE");
        testAccountDTO.setStatusId(1L);
    }

    @Test
    void testGetAllAccounts_Success() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountMapper.findAll()).thenReturn(accounts);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        List<AccountDTO> result = accountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountMapper, times(1)).findAll();
    }

    @Test
    void testGetAllAccounts_EmptyList_ThrowsException() {
        // Arrange
        when(accountMapper.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.getAllAccounts());
    }

    @Test
    void testGetAccountById_Success() {
        // Arrange
        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        AccountDTO result = accountService.getAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        verify(accountMapper, times(1)).findById(1L);
    }


    @Test
    void testGetAccountsByUserId_Success() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountMapper.findByUserId(1L)).thenReturn(accounts);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        List<AccountDTO> result = accountService.getAccountsByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountMapper, times(1)).findByUserId(1L);
    }

    @Test
    void testGetAccountsByUserId_NoAccounts_ThrowsException() {
        // Arrange
        when(accountMapper.findByUserId(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.getAccountsByUserId(999L));
    }

    @Test
    void testGetAccountProfile_Success() {
        // Arrange
        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        AccountProfileDTO result = accountService.getAccountProfile(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        verify(accountMapper, times(1)).findById(1L);
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        when(accountMapper.insert(testAccount)).thenReturn(1);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        AccountDTO result = accountService.createAccount(testAccount);

        // Assert
        assertNotNull(result);
        verify(accountMapper, times(1)).insert(testAccount);
    }

    @Test
    void testCreateAccount_InvalidUserId_ThrowsException() {
        // Arrange
        testAccount.setUserId(null);
    
        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.createAccount(testAccount));
        
        // Verify that the mapper was never called (validation happened first)
        verify(accountMapper, never()).insert(any());
    }

    @Test
    void testUpdateAccount_Success() {
        // Arrange
        Account updatedAccount = new Account();
        updatedAccount.setCurrency("EUR");
        updatedAccount.setSettledCash(new BigDecimal("15000.00"));

        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(accountMapper.update(any())).thenReturn(1);
        when(accountStatusMapper.findById(1L)).thenReturn(testStatus);
        when(accountTypeMapper.findById(1L)).thenReturn(testType);

        // Act
        AccountDTO result = accountService.updateAccount(1L, updatedAccount);

        // Assert
        assertNotNull(result);
        verify(accountMapper, times(1)).update(any());
    }

    @Test
    void testUpdateAccount_NotFound_ThrowsException() {
        // Arrange
        when(accountMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.updateAccount(999L, testAccount));
    }

    @Test
    void testDeleteAccount_Success() {
        // Arrange
        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(accountMapper.delete(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> accountService.deleteAccount(1L));

        // Assert
        verify(accountMapper, times(1)).delete(1L);
    }

    @Test
    void testDeleteAccount_NotFound_ThrowsException() {
        // Arrange
        when(accountMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.deleteAccount(999L));
    }
}
