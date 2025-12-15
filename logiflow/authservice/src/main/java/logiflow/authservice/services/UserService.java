package logiflow.authservice.services;

import logiflow.authservice.dto.request.LoginRequest;
import logiflow.authservice.dto.request.RegisterRequest;
import logiflow.authservice.dto.response.AuthResponse;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void revokeRefreshToken(String refreshToken);
}

