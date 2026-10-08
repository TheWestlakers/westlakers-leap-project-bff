package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.dtos.UserProfileDTO;
import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.entities.UserStatus;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.RoleMapper;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.mappers.UserMapper;
import com.westlakers.leap_bff.mappers.UserStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserStatusMapper userStatusMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserCredentialsMapper credentialsMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserStatus testStatus;
    private Role testRole;
    private UserCredentials testCredentials;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testStatus = new UserStatus();
        testStatus.setUserStatusId(1L);
        testStatus.setStatusName("ACTIVE");

        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName("TRADER");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("5551234567");
        testUser.setTaxId("123456789");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testUser.setStatusId(1L);

        testCredentials = new UserCredentials();
        testCredentials.setCredentialId(1L);
        testCredentials.setUserId(1L);
        testCredentials.setRoleId(1L);
        testCredentials.setUsername("jdoe");
        testCredentials.setEmail("john.doe@example.com");
        testCredentials.setActive(true);

        testUserDTO = new UserDTO();
        testUserDTO.setUserId(1L);
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setPhoneNumber("5551234567");
        testUserDTO.setTaxId("123456789");
    }

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userMapper.findAll()).thenReturn(users);
        when(userStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_EmptyList_ThrowsException() {
        // Arrange
        when(userMapper.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getAllUsers());
        assertEquals(ErrorCode.EMPTY_RESULTS, exception.getErrorCode());
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userMapper, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_InvalidId_ThrowsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(0L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testGetUserById_NegativeId_ThrowsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(-1L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testGetUserById_NotFound_ThrowsException() {
        // Arrange
        when(userMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(999L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testGetUserProfile_Success() {
        // Arrange
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userStatusMapper.findById(1L)).thenReturn(testStatus);
        when(roleMapper.findById(anyLong())).thenReturn(testRole);
        when(credentialsMapper.findByUserId(1L)).thenReturn(testCredentials);

        // Act
        UserProfileDTO result = userService.getUserProfile(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("jdoe", result.getUsername());
        assertEquals("TRADER", result.getRole());
        verify(userMapper, times(1)).findById(1L);
    }

    @Test
    void testGetUserProfile_InvalidId_ThrowsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserProfile(0L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testGetUserProfile_UserNotFound_ThrowsException() {
        // Arrange
        when(userMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserProfile(999L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testCreateUser_ValidData() {
        // Arrange
        testUserDTO.setStatusId(1L);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(userStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        UserDTO result = userService.createUser(testUserDTO);

        // Assert
        assertNotNull(result);
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testCreateUser_InvalidStatusId_ThrowsException() {
        // Arrange
        testUserDTO.setStatusId(999L);
        when(userStatusMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(testUserDTO));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setFirstName("Jane");
        updatedUserDTO.setLastName("Smith");
        updatedUserDTO.setPhoneNumber("5555678");
        updatedUserDTO.setTaxId("987654321");
        updatedUserDTO.setStatusId(1L);

        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.update(any())).thenReturn(1);
        when(userStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        // Assert
        assertNotNull(result);
        verify(userMapper, times(1)).update(any());
    }

    @Test
    void testUpdateUser_InvalidId_ThrowsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.updateUser(0L, testUserDTO));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testUpdateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.updateUser(999L, testUserDTO));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.delete(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        // Assert
        verify(userMapper, times(1)).delete(1L);
    }

    @Test
    void testDeleteUser_InvalidId_ThrowsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(0L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void testDeleteUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(999L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
