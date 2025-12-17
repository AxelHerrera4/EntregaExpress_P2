package logiflow.authservice.utils;

import logiflow.authservice.model.Role;
import logiflow.authservice.model.RoleName;
import logiflow.authservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldGenerateValidAccessToken() {
        User user = createTestUser("testuser");
        String token = jwtUtils.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        User user = createTestUser("testuser");
        String token = jwtUtils.generateRefreshToken(user);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void shouldValidateValidToken() {
        User user = createTestUser("testuser");
        String token = jwtUtils.generateAccessToken(user);

        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        User user = createTestUser("testuser");
        String token = jwtUtils.generateAccessToken(user);

        String username = jwtUtils.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void shouldExtractRolesFromToken() {
        User user = createTestUser("testuser");
        String token = jwtUtils.generateAccessToken(user);

        List<String> roles = jwtUtils.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("CLIENTE"));
    }

    @Test
    void shouldReturnNullForMalformedToken() {
        assertNull(jwtUtils.extractUsername("malformed"));
    }

    @Test
    void shouldReturnEmptyRolesForInvalidToken() {
        List<String> roles = jwtUtils.extractRoles("invalid.token");
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    private User createTestUser(String username) {
        Role role = Role.builder()
                .id(1L)
                .name(RoleName.CLIENTE)
                .build();

        return User.builder()
                .id(1L)
                .username(username)
                .email(username + "@test.com")
                .password("hashedpassword")
                .roles(Set.of(role))
                .build();
    }
}
