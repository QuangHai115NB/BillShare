package vn.backend.backend.service;

import vn.backend.backend.common.dto.request.LoginRequest;
import vn.backend.backend.common.dto.request.RegisterRequest;
import vn.backend.backend.common.dto.response.LoginResponse;
import vn.backend.backend.common.dto.response.RegisterResponse;

public interface AuthService {
    void requestRegistrationOtp(String email);
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
