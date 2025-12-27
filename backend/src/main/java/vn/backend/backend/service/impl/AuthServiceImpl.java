package vn.backend.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.backend.backend.common.dto.request.LoginRequest;
import vn.backend.backend.common.dto.request.RegisterRequest;
import vn.backend.backend.common.dto.response.LoginResponse;
import vn.backend.backend.common.dto.response.RegisterResponse;
import vn.backend.backend.config.JwtService;
import vn.backend.backend.exception.BadRequestException;
import vn.backend.backend.model.User;
import vn.backend.backend.repository.UserRepository;
import vn.backend.backend.service.AuthService;
import vn.backend.backend.service.EmailService; // Import mới
import vn.backend.backend.service.OtpManager;   // Import mới

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpManager otpManager;    // Tiêm vào
    private final EmailService emailService; // Tiêm vào

    // Bước 1: Gọi hàm này khi người dùng nhấn "Gửi mã"
    @Override
    public void requestRegistrationOtp(String email) {
        // Kiểm tra email tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã tồn tại");
        }

        // Tạo OTP và lưu vào bộ nhớ tạm
        String otp = otpManager.generateAndSaveOtp(email);

        // Gửi qua Gmail
        emailService.sendOtpEmail(email, otp);
    }

    // Bước 2: Gọi hàm này khi người dùng nhấn "Đăng ký" (kèm mã OTP trong request)
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Kiểm tra mã OTP trước tiên
        boolean isOtpValid = otpManager.validateOtp(request.getEmail(), request.getOtp());
        if (!isOtpValid) {
            throw new BadRequestException("Mã xác thực không chính xác hoặc đã hết hạn");
        }

        // 2. Kiểm tra lại email (đề phòng trường hợp đăng ký kép giữa lúc gửi OTP)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        // 3. Tạo User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true); // Đánh dấu tài khoản đã kích hoạt vì OTP đúng

        User savedUser = userRepository.save(user);

        // 4. Xóa OTP sau khi dùng thành công
        otpManager.clearOtp(request.getEmail());

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFullName()
        );
    }
}