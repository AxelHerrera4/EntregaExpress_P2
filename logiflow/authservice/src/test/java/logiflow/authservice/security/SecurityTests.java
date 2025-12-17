package logiflow.authservice.security;

import logiflow.authservice.model.Role;
import logiflow.authservice.model.RoleName;
import logiflow.authservice.model.User;
import logiflow.authservice.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void unauthenticatedRequestShouldBe401() throws Exception {
        mockMvc.perform(get("/api/protected/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedRequestShouldBe200() throws Exception {
        String token = buildTokenWithRoles("user1", List.of("CLIENTE"));
        mockMvc.perform(get("/api/protected/me")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void forbiddenWhenMissingRole() throws Exception {
        String token = buildTokenWithRoles("user1", List.of("CLIENTE"));
        mockMvc.perform(get("/api/protected/admin-only")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointWithAdminRoleShouldBe200() throws Exception {
        String token = buildTokenWithRoles("admin", List.of("ADMINISTRADOR_SISTEMA"));
        mockMvc.perform(get("/api/protected/admin-only")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void invalidTokenShouldBe401() throws Exception {
        mockMvc.perform(get("/api/protected/me")
                        .header("Authorization", "Bearer invalid.token.here")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private String buildTokenWithRoles(String username, List<String> roles) {
        Set<Role> roleSet = roles.stream()
                .map(r -> Role.builder()
                        .name(RoleName.valueOf(r))
                        .build())
                .collect(Collectors.toSet());
        
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .roles(roleSet)
                .build();
        
        return jwtUtils.generateAccessToken(user);
    }
}
