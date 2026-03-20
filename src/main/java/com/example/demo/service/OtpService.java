package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, OtpDetails> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String key) {
        String otp = String.format("%06d", random.nextInt(999999));
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000;
        otpCache.put(key, new OtpDetails(otp, expiryTime));
        
        System.out.println("\n=====================================");
        System.out.println(">>> OTP for " + key + ": " + otp + " <<<");
        System.out.println("=====================================\n");
        
        return otp;
    }

    public boolean verifyOtp(String key, String otp) {
        OtpDetails details = otpCache.get(key);
        if (details == null) {
            return false;
        }
        if (System.currentTimeMillis() > details.expiryTime) {
            otpCache.remove(key);
            return false;
        }
        if (details.otp.equals(otp)) {
            otpCache.remove(key);
            return true;
        }
        return false;
    }

    private static class OtpDetails {
        String otp;
        long expiryTime;

        OtpDetails(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}
