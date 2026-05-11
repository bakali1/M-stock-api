package com.mstock.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mstock.api.payload.Request.AuthResponse;
import com.mstock.api.payload.Request.LoginRequest;
import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v0/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<GeneralResponde<AuthResponse>> login(@RequestBody LoginRequest request) {
        GeneralResponde<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponde<AuthResponse>> register(@RequestBody RegisterRequest request) {
        GeneralResponde<AuthResponse> response = authService.register(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
