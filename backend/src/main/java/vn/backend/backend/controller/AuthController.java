package vn.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.backend.backend.common.dto.request.LoginRequest;
import vn.backend.backend.common.dto.request.RegisterRequest;
import vn.backend.backend.common.dto.response.LoginResponse;
import vn.backend.backend.common.dto.response.RegisterResponse;
import vn.backend.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        authService.requestRegistrationOtp(email); // Thêm chữ "Otp" vào cuối
        return ResponseEntity.ok("Mã OTP đã được gửi về email.");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
