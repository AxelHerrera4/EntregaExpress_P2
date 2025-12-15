package logiflow.authservice.security;

import logiflow.authservice.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtils jwtUtils;

    @Test
    void unauthenticatedRequestShouldBe401() throws Exception {
        mockMvc.perform(get("/api/protected/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbiddenWhenMissingRole() throws Exception {
        // Build a token without ADMINISTRADOR_SISTEMA role
        var token = buildTokenWithRoles("user1", java.util.List.of("CLIENTE"));
        mockMvc.perform(get("/api/protected/admin-only")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private String buildTokenWithRoles(String username, java.util.List<String> roles) {
        java.util.Set<logiflow.authservice.model.Role> roleSet = roles.stream()
                .map(r -> logiflow.authservice.model.Role.builder()
                        .name(logiflow.authservice.model.RoleName.valueOf(r))
                        .build())
                .collect(java.util.stream.Collectors.toSet());
        logiflow.authservice.model.User u = logiflow.authservice.model.User.builder()
                .username(username)
                .roles(roleSet)
                .build();
        return jwtUtils.generateAccessToken(u);
    }
}
