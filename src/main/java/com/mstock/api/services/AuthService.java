package com.mstock.api.services;

import com.mstock.api.payload.Request.AuthResponse;
import com.mstock.api.payload.Request.LoginRequest;
import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;

public interface AuthService {
    GeneralResponde<AuthResponse> login(LoginRequest request);
    GeneralResponde<AuthResponse> register(RegisterRequest request);
}
