package vn.backend.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.backend.backend.common.dto.request.LoginRequest;
import vn.backend.backend.common.dto.request.RegisterRequest;
import vn.backend.backend.common.dto.response.LoginResponse;
import vn.backend.backend.common.dto.response.RegisterResponse;
import vn.backend.backend.config.JwtService;
import vn.backend.backend.exception.BadRequestException;
import vn.backend.backend.model.User;
import vn.backend.backend.repository.UserRepository;
import vn.backend.backend.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        );
    }
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user theo email, nếu không thấy thì báo lỗi
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không chính xác"));

        // 2. So sánh mật khẩu người dùng gửi lên với mật khẩu đã mã hóa trong DB
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        // 3. Tạo JWT Token (Sử dụng JwtService)
        String token = jwtService.generateToken(user);

        // 4. Trả về kết quả
        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFullName()
        );
    }
}