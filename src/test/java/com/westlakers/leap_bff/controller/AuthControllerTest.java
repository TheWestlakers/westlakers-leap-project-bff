// package com.westlakers.leap_bff.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.westlakers.leap_bff.dtos.LoginRequest;
// import com.westlakers.leap_bff.dtos.LoginResponse;
// import com.westlakers.leap_bff.security.JwtService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// /**
//  * AuthController Tests
//  * Note: Full integration tests with MockMvc require Spring Boot test dependencies.
//  * This version focuses on basic controller logic validation.
//  */
// class AuthControllerTest {

//     @Mock
//     private JwtService jwtService;

//     private AuthController authController;
//     private LoginRequest testLoginRequest;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         authController = new AuthController(jwtService);

//         // Setup test data
//         testLoginRequest = new LoginRequest();
//         testLoginRequest.setUsername("jdoe");
//         testLoginRequest.setPassword("password123");
//     }

//     @Test
//     void testLogin_ValidCredentials_ReturnsToken() {
//         // Arrange
//         String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
//         when(jwtService.generateToken("jdoe")).thenReturn(expectedToken);

//         // Act
//         LoginResponse response = authController.login(testLoginRequest);

//         // Assert
//         assertNotNull(response);
//         assertEquals(expectedToken, response.getToken());
//         verify(jwtService, times(1)).generateToken("jdoe");
//     }

//     @Test
//     void testLogin_CallsJwtService() {
//         // Arrange
//         when(jwtService.generateToken("jdoe")).thenReturn("token");

//         // Act
//         authController.login(testLoginRequest);

//         // Assert
//         verify(jwtService, times(1)).generateToken("jdoe");
//     }

//     @Test
//     void testValidateToken_ValidToken_ReturnsTrue() {
//         // Arrange
//         when(jwtService.validateToken("valid.token")).thenReturn(true);

//         // Act
//         boolean result = authController.validateToken("Bearer valid.token");

//         // Assert
//         assertTrue(result);
//         verify(jwtService, times(1)).validateToken("valid.token");
//     }

//     @Test
//     void testValidateToken_InvalidToken_ReturnsFalse() {
//         // Arrange
//         when(jwtService.validateToken("invalid.token")).thenReturn(false);

//         // Act
//         boolean result = authController.validateToken("Bearer invalid.token");

//         // Assert
//         assertFalse(result);
//     }

//     @Test
//     void testValidateToken_MissingBearerPrefix_ReturnsFalse() {
//         // Arrange
//         String authHeaderWithoutBearer = "token-without-bearer";

//         // Act
//         boolean result = authController.validateToken(authHeaderWithoutBearer);

//         // Assert
//         assertFalse(result);
//         verify(jwtService, never()).validateToken(any());
//     }

//     @Test
//     void testValidateToken_NullAuthHeader_ReturnsFalse() {
//         // Act
//         boolean result = authController.validateToken(null);

//         // Assert
//         assertFalse(result);
//         verify(jwtService, never()).validateToken(any());
//     }
// }
