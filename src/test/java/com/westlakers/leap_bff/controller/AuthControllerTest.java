package com.westlakers.leap_bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westlakers.leap_bff.dtos.CurrentUserResponse;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;
import com.westlakers.leap_bff.dtos.RefreshTokenRequest;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.exceptions.GlobalExceptionHandler;
import com.westlakers.leap_bff.security.JwtService;
import com.westlakers.leap_bff.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

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
	private AuthService authService;

	@Mock
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		AuthController controller = new AuthController(authService, jwtService);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new GlobalExceptionHandler())
			.setValidator(validator)
			.build();
	}

	@Test
	void login_ValidCredentials_ReturnsToken() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("jdoe@example.com");
		request.setPassword("password123");

		when(authService.authenticateAndIssueToken(any(LoginRequest.class))).thenReturn("jwt-token");
		when(jwtService.generateRefreshToken("jdoe@example.com")).thenReturn("refresh-token");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value("jwt-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"));

		verify(authService, times(1)).authenticateAndIssueToken(any(LoginRequest.class));
	}

	@Test
	void refresh_ValidRefreshToken_ReturnsAccessToken() throws Exception {
		when(jwtService.validateRefreshToken("refresh-token")).thenReturn(true);
		when(jwtService.getEmailFromToken("refresh-token")).thenReturn("jdoe@example.com");
		when(jwtService.generateToken("jdoe@example.com")).thenReturn("new-jwt-token");

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"refresh-token\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").value("new-jwt-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"));
	}

	@Test
	void refresh_InvalidRefreshToken_ReturnsUnauthorized() throws Exception {
		when(jwtService.validateRefreshToken("invalid-token")).thenReturn(false);

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"invalid-token\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.errorCode").value("ERR_042"));
	}

	@Test
	void login_InvalidPayload_ReturnsBadRequest() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setPassword("password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(authService, never()).authenticateAndIssueToken(any(LoginRequest.class));
	}

	@Test
	void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("jdoe@example.com");
		request.setPassword("wrong-pass");

		when(authService.authenticateAndIssueToken(any(LoginRequest.class)))
			.thenThrow(new ApiException(ErrorCode.INVALID_CREDENTIALS));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.errorCode").value("ERR_042"));
	}

	@Test
	void register_ValidPayload_CreatesUser() throws Exception {
				String requestJson = """
						{
							"email": "new.user@example.com",
							"password": "StrongPassword123!",
							"firstName": "New",
							"lastName": "User",
							"phoneNumber": "5551234567",
							"taxId": "123456789",
							"dateOfBirth": "1995-05-10",
							"accountTypeId": 1,
							"settledCash": 10000.00
						}
						""";

		when(authService.register(any(RegisterRequest.class)))
			.thenReturn(new RegisterResponse(42L, "new.user@example.com", "new.user@example.com"));

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
			.content(requestJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(42))
			.andExpect(jsonPath("$.email").value("new.user@example.com"))
			.andExpect(jsonPath("$.username").value("new.user@example.com"));

		verify(authService, times(1)).register(any(RegisterRequest.class));
	}

	@Test
	void register_RandomUsername_IsRejected() throws Exception {
		String requestJson = """
				{
					"email": "new.user@example.com",
					"username": "random-user",
					"password": "StrongPassword123!",
					"firstName": "New",
					"lastName": "User",
					"phoneNumber": "5551234567",
					"dateOfBirth": "1995-05-10"
				}
				""";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isBadRequest());

		verify(authService, never()).register(any(RegisterRequest.class));
	}

	@Test
	void register_NegativeSettledCash_IsRejected() throws Exception {
		String requestJson = """
				{
					"email": "new.user@example.com",
					"password": "StrongPassword123!",
					"firstName": "New",
					"lastName": "User",
					"phoneNumber": "5551234567",
					"dateOfBirth": "1995-05-10",
					"accountTypeId": 1,
					"settledCash": -50.00
				}
				""";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isBadRequest());

		verify(authService, never()).register(any(RegisterRequest.class));
	}

	@Test
	void register_MissingAccountType_IsRejected() throws Exception {
		String requestJson = """
				{
					"email": "new.user@example.com",
					"password": "StrongPassword123!",
					"firstName": "New",
					"lastName": "User",
					"phoneNumber": "5551234567",
					"dateOfBirth": "1995-05-10",
					"settledCash": 100.00
				}
				""";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isBadRequest());

		verify(authService, never()).register(any(RegisterRequest.class));
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

	@Test
	void me_Authenticated_ReturnsCurrentUser() throws Exception {
		when(authService.getCurrentUser("jdoe@example.com"))
			.thenReturn(new CurrentUserResponse(5L, "jdoe@example.com", "John", "Doe", 2L, List.of()));

		mockMvc.perform(get("/api/auth/me")
				.principal(new UsernamePasswordAuthenticationToken("jdoe@example.com", null, List.of())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(5))
			.andExpect(jsonPath("$.firstName").value("John"))
			.andExpect(jsonPath("$.roleId").value(2))
			.andExpect(jsonPath("$.accounts").isArray());
	}

	@Test
	void me_Unauthenticated_ReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized());

		verify(authService, never()).getCurrentUser(any());
	}
}
