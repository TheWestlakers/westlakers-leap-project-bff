// package com.westlakers.leap_bff.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.westlakers.leap_bff.dtos.LoginRequest;
// import com.westlakers.leap_bff.dtos.LoginResponse;
// import com.westlakers.leap_bff.security.JwtService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.hamcrest.Matchers.containsString;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// /**
//  * AuthController Integration Tests
//  * Full Spring Boot integration tests using MockMvc to test the controller
//  * in the context of the Spring application.
//  */
// @SpringBootTest
// @AutoConfigureMockMvc
// class AuthControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private JwtService jwtService;

//     private LoginRequest testLoginRequest;

//     @BeforeEach
//     void setUp() {
//         // Setup test data
//         testLoginRequest = new LoginRequest();
//         testLoginRequest.setUsername("jdoe");
//         testLoginRequest.setPassword("password123");
//     }

//     @Test
//     void testLogin_ValidCredentials_ReturnsToken() throws Exception {
//         // Arrange
//         String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
//         when(jwtService.generateToken("jdoe")).thenReturn(expectedToken);

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(testLoginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").value(expectedToken));

//         verify(jwtService, times(1)).generateToken("jdoe");
//     }

//     @Test
//     void testLogin_CallsJwtService() throws Exception {
//         // Arrange
//         when(jwtService.generateToken("jdoe")).thenReturn("token");

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(testLoginRequest)))
//                 .andExpect(status().isOk());

//         verify(jwtService, times(1)).generateToken("jdoe");
//     }

//     @Test
//     void testValidateToken_ValidToken_ReturnsTrue() throws Exception {
//         // Arrange
//         when(jwtService.validateToken("valid.token")).thenReturn(true);

//         // Act & Assert
//         mockMvc.perform(get("/api/auth/validate")
//                 .header("Authorization", "Bearer valid.token"))
//                 .andExpect(status().isOk());

//         verify(jwtService, times(1)).validateToken("valid.token");
//     }

//     @Test
//     void testValidateToken_InvalidToken_ReturnsFalse() throws Exception {
//         // Arrange
//         when(jwtService.validateToken("invalid.token")).thenReturn(false);

//         // Act & Assert
//         mockMvc.perform(get("/api/auth/validate")
//                 .header("Authorization", "Bearer invalid.token"))
//                 .andExpect(status().isUnauthorized());
//     }

//     @Test
//     void testValidateToken_MissingBearerPrefix_ReturnsFalse() throws Exception {
//         // Arrange
//         String authHeaderWithoutBearer = "token-without-bearer";

//         // Act & Assert
//         mockMvc.perform(get("/api/auth/validate")
//                 .header("Authorization", authHeaderWithoutBearer))
//                 .andExpect(status().isUnauthorized());

//         verify(jwtService, never()).validateToken(any());
//     }

//     @Test
//     void testValidateToken_NullAuthHeader_ReturnsFalse() throws Exception {
//         // Act & Assert
//         mockMvc.perform(get("/api/auth/validate"))
//                 .andExpect(status().isUnauthorized());

//         verify(jwtService, never()).validateToken(any());
//     }
// }
