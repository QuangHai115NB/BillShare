package vn.backend.backend.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpManager {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class OtpData {
        private String code;
        private LocalDateTime expiryTime;
    }

    public String generateAndSaveOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));
        return otp;
    }

    public boolean validateOtp(String email, String code) {
        OtpData data = otpStorage.get(email);
        if (data == null || data.getExpiryTime().isBefore(LocalDateTime.now())) {
            if (data != null) otpStorage.remove(email);
            return false;
        }
        return data.getCode().equals(code);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
}