package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.dtos.UserProfileDTO;
import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserStatus;
import com.westlakers.leap_bff.mappers.RoleMapper;
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

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserStatus testStatus;
    private Role testRole;

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

        testUserDTO = new UserDTO();
        testUserDTO.setUserId(1L);
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setPhoneNumber("5551234567");
        testUserDTO.setTaxId("123456789");
    }

//     @Test
//     void testGetAllUsers_Success() {
//         // Arrange
//         List<User> users = Arrays.asList(testUser);
//         when(userMapper.findAll()).thenReturn(users);
//         when(userStatusMapper.findById(1L)).thenReturn(testStatus);

//         // Act
//         List<UserDTO> result = userService.getAllUsers();

//         // Assert
//         assertNotNull(result);
//         assertEquals(1, result.size());
//         verify(userMapper, times(1)).findAll();
//     }

//     @Test
//     void testGetAllUsers_EmptyList_ThrowsException() {
//         // Arrange
//         when(userMapper.findAll()).thenReturn(Collections.emptyList());

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.getAllUsers());
//     }

//     @Test
//     void testGetUserById_Success() {
//         // Arrange
//         when(userMapper.findById(1L)).thenReturn(testUser);
//         when(userStatusMapper.findById(1L)).thenReturn(testStatus);

//         // Act
//         UserDTO result = userService.getUserById(1L);

//         // Assert
//         assertNotNull(result);
//         assertEquals(1L, result.getUserId());
//         assertEquals("jdoe", result.getUsername());
//         verify(userMapper, times(1)).findById(1L);
//     }

//     @Test
//     void testGetUserById_InvalidId_ThrowsException() {
//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> userService.getUserById(0L));
//     }

//     @Test
//     void testGetUserById_NegativeId_ThrowsException() {
//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> userService.getUserById(-1L));
//     }

//     @Test
//     void testGetUserById_NotFound_ThrowsException() {
//         // Arrange
//         when(userMapper.findById(999L)).thenReturn(null);

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.getUserById(999L));
//     }

//     @Test
//     void testGetUserProfile_Success() {
//         // Arrange
//         when(userMapper.findById(1L)).thenReturn(testUser);
//         when(userStatusMapper.findById(1L)).thenReturn(testStatus);
//         when(roleMapper.findById(anyLong())).thenReturn(testRole);

//         // Act
//         UserProfileDTO result = userService.getUserProfile(1L);

//         // Assert
//         assertNotNull(result);
//         assertEquals(1L, result.getUserId());
//         assertEquals("jdoe", result.getUsername());
//         verify(userMapper, times(1)).findById(1L);
//     }

//     @Test
//     void testGetUserProfile_InvalidId_ThrowsException() {
//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> userService.getUserProfile(0L));
//     }

//     @Test
//     void testGetUserProfile_UserNotFound_ThrowsException() {
//         // Arrange
//         when(userMapper.findById(999L)).thenReturn(null);

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.getUserProfile(999L));
//     }

//     @Test
//     void testCreateUser_ValidData() {
//         // Arrange
//         when(userMapper.insert(any(User.class))).thenReturn(1);
//         when(userStatusMapper.findById(1L)).thenReturn(testStatus);

//         // Act
//         UserDTO result = userService.createUser(testUserDTO);

//         // Assert
//         assertNotNull(result);
//         verify(userMapper, times(1)).insert(any(User.class));
//     }

//     @Test
//     void testCreateUser_InvalidStatusId_ThrowsException() {
//         // Arrange
//         testUserDTO.setUserStatusId(999L);
//         when(userStatusMapper.findById(999L)).thenReturn(null);

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.createUser(testUserDTO));
//     }

//     @Test
//     void testUpdateUser_Success() {
//         // Arrange
//         UserDTO updatedUserDTO = new UserDTO();
//         updatedUserDTO.setFirstName("Jane");
//         updatedUserDTO.setLastName("Smith");
//         updatedUserDTO.setPhoneNumber("555-5678");

//         when(userMapper.findById(1L)).thenReturn(testUser);
//         when(userMapper.update(any())).thenReturn(1);
//         when(userStatusMapper.findById(1L)).thenReturn(testStatus);

//         // Act
//         UserDTO result = userService.updateUser(1L, updatedUserDTO);

//         // Assert
//         assertNotNull(result);
//         verify(userMapper, times(1)).update(any());
//     }

//     @Test
//     void testUpdateUser_InvalidId_ThrowsException() {
//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> userService.updateUser(0L, testUserDTO));
//     }

//     @Test
//     void testUpdateUser_UserNotFound_ThrowsException() {
//         // Arrange
//         when(userMapper.findById(999L)).thenReturn(null);

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.updateUser(999L, testUserDTO));
//     }

//     @Test
//     void testDeleteUser_Success() {
//         // Arrange
//         when(userMapper.findById(1L)).thenReturn(testUser);
//         when(userMapper.delete(1L)).thenReturn(1);

//         // Act
//         assertDoesNotThrow(() -> userService.deleteUser(1L));

//         // Assert
//         verify(userMapper, times(1)).delete(1L);
//     }

//     @Test
//     void testDeleteUser_InvalidId_ThrowsException() {
//         // Act & Assert
//         assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(0L));
//     }

//     @Test
//     void testDeleteUser_UserNotFound_ThrowsException() {
//         // Arrange
//         when(userMapper.findById(999L)).thenReturn(null);

//         // Act & Assert
//         assertThrows(IllegalStateException.class, () -> userService.deleteUser(999L));
//     }
}
