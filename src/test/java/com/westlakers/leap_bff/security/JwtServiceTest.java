package com.westlakers.leap_bff.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String testSecret;
    private String validToken;
    private String testUsername;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test secret (must be 32+ characters for HmacSHA256)
        testSecret = "ThisIsATestSecretKeyThatIsLongEnoughForHS256";
        testUsername = "testuser";

        // Inject the secret via reflection since it's typically injected from properties
        ReflectionTestUtils.setField(jwtService, "sharedSecret", testSecret);

        // Generate a valid token for testing
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        validToken = Jwts.builder()
            .subject(testUsername)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour from now
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtService.generateToken(testUsername);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_ContainsUsername() {
        // Act
        String token = jwtService.generateToken(testUsername);

        // Assert
        String username = jwtService.getUsernameFromToken(token);
        assertEquals(testUsername, username);
    }

    @Test
    void testGenerateToken_TokenIsExpirable() {
        // Act
        String token = jwtService.generateToken(testUsername);

        // Assert - Token should be valid initially
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Act
        boolean isValid = jwtService.validateToken(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_TamperedSignature_ReturnsFalse() {
        // Arrange
        // Take valid token and modify it
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        // Act
        boolean isValid = jwtService.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetUsernameFromToken_ValidToken() {
        // Act
        String username = jwtService.getUsernameFromToken(validToken);

        // Assert
        assertEquals(testUsername, username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtService.getUsernameFromToken(invalidToken));
    }

    @Test
    void testGetUsernameFromToken_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.getUsernameFromToken(null));
    }

    @Test
    void testGenerateAndValidateToken_RoundTrip() {
        // Arrange
        String username = "roundtripuser";

        // Act
        String token = jwtService.generateToken(username);
        boolean isValid = jwtService.validateToken(token);
        String extractedUsername = jwtService.getUsernameFromToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGenerateToken_MultipleTokens_Different() throws InterruptedException {
        // Act
        String token1 = jwtService.generateToken(testUsername);
        Thread.sleep(1001); // Ensure different timestamps (1+ second)
        String token2 = jwtService.generateToken(testUsername);

        // Assert - Tokens should be different (different issuedAt timestamps)
        assertNotEquals(token1, token2);
    }

    @Test
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Act
        String token1 = jwtService.generateToken("user1");
        String token2 = jwtService.generateToken("user2");

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtService.getUsernameFromToken(token1));
        assertEquals("user2", jwtService.getUsernameFromToken(token2));
    }
}
