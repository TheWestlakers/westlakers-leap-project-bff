package com.westlakers.leap_bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		AuthController controller = new AuthController(jwtService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.build();
	}

	@Test
	void login_ValidCredentials_ReturnsToken() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("jdoe@example.com");
		request.setPassword("password123");

		when(jwtService.generateToken("jdoe@example.com")).thenReturn("jwt-token");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value("jwt-token"));

		verify(jwtService, times(1)).generateToken("jdoe@example.com");
	}

	@Test
	void validateToken_WithBearerHeader_ReturnsTrue() throws Exception {
		when(jwtService.validateToken("valid.token")).thenReturn(true);

		mockMvc.perform(get("/api/auth/validate")
				.header("Authorization", "Bearer valid.token"))
			.andExpect(status().isOk())
			.andExpect(content().string("true"));

		verify(jwtService, times(1)).validateToken("valid.token");
	}

	@Test
	void validateToken_WithoutBearerPrefix_ReturnsFalse() throws Exception {
		mockMvc.perform(get("/api/auth/validate")
				.header("Authorization", "token-without-bearer"))
			.andExpect(status().isOk())
			.andExpect(content().string("false"));

		verify(jwtService, never()).validateToken(any());
	}
}
