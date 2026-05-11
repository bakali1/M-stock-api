package com.mstock.api.services.imp;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mstock.api.Enum.UserRoleEnum;
import com.mstock.api.Mappers.UserMapper;
import com.mstock.api.config.jwt.JwtService;
import com.mstock.api.entities.User;
import com.mstock.api.payload.Request.AuthResponse;
import com.mstock.api.payload.Request.LoginRequest;
import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.UserRepository;
import com.mstock.api.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GeneralResponde<AuthResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(
                user,
                Map.of("role", user.getRole() != null ? user.getRole().name() : null,
                        "username", user.getUsername()));

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .build();

        return GeneralResponde.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .msg("login ok")
                .data(authResponse)
                .build();
    }

    @Override
    public GeneralResponde<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return GeneralResponde.<AuthResponse>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .msg("user with this email already exists")
                    .build();
        }

        User user = new User();
        userMapper.updateUserFromRequest(request, user);
        if (user.getRole() == null) {
            user.setRole(UserRoleEnum.VIEWER);
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        String token = jwtService.generateToken(
                user,
                Map.of("role", user.getRole() != null ? user.getRole().name() : null,
                        "email", user.getEmail()));

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .build();

        return GeneralResponde.<AuthResponse>builder()
                .status(HttpStatus.CREATED.value())
                .msg("register ok")
                .data(authResponse)
                .build();
    }
}
