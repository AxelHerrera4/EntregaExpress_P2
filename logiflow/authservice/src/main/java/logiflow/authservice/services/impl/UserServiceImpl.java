package logiflow.authservice.services.impl;

import logiflow.authservice.dto.mapper.UserMapper;
import logiflow.authservice.dto.request.LoginRequest;
import logiflow.authservice.dto.request.RegisterRequest;
import logiflow.authservice.dto.response.AuthResponse;
import logiflow.authservice.model.Role;
import logiflow.authservice.model.RoleName;
import logiflow.authservice.model.User;
import logiflow.authservice.repositories.RoleRepository;
import logiflow.authservice.repositories.UserRepository;
import logiflow.authservice.services.UserService;
import logiflow.authservice.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(RoleName.CLIENTE)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.CLIENTE).build()));
            roles.add(defaultRole);
        } else {
            for (String roleNameStr : request.getRoles()) {
                RoleName rn = RoleName.valueOf(roleNameStr);
                Role role = roleRepository.findByName(rn)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(rn).build()));
                roles.add(role);
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        String access = jwtUtils.generateAccessToken(user);
        String refresh = jwtUtils.generateRefreshToken(user);
        user.setRefreshToken(refresh);
        userRepository.save(user);

        return UserMapper.toAuthResponse(user, access, refresh);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String access = jwtUtils.generateAccessToken(user);
        String refresh = jwtUtils.generateRefreshToken(user);
        user.setRefreshToken(refresh);
        userRepository.save(user);
        return UserMapper.toAuthResponse(user, access, refresh);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token revoked or mismatched");
        }
        String access = jwtUtils.generateAccessToken(user);
        String newRefresh = jwtUtils.generateRefreshToken(user);
        user.setRefreshToken(newRefresh);
        userRepository.save(user);
        return UserMapper.toAuthResponse(user, access, newRefresh);
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) return;
        String username = jwtUtils.extractUsername(refreshToken);
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setRefreshToken(null);
            userRepository.save(u);
        });
    }
}

