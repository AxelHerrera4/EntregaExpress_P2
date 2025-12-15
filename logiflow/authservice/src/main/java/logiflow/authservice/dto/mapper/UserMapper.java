package logiflow.authservice.dto.mapper;

import logiflow.authservice.dto.response.AuthResponse;
import logiflow.authservice.model.Role;
import logiflow.authservice.model.User;

import java.util.stream.Collectors;

public class UserMapper {
    public static AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet()))
                .build();
    }
}

