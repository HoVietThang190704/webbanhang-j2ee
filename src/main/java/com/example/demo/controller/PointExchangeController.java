package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.IUserRepository;
import com.example.demo.service.OtpService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/point-exchange")
@RequiredArgsConstructor
public class PointExchangeController {
    
    private final OtpService otpService;
    private final UserService userService;
    private final IUserRepository userRepository;

    @GetMapping
    public String showExchangeForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("loyaltyPoints", user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0);
        return "users/point-exchange";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("pointsToExchange") int points, 
                          @RequestParam("method") String method,
                          RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow();
        
        int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
        if (points <= 0 || points > currentPoints) {
            redirectAttributes.addFlashAttribute("error", "Số điểm không hợp lệ hoặc bạn không đủ điểm!");
            return "redirect:/point-exchange";
        }
        
        String identifier = auth.getName();
        otpService.generateOtp(identifier);
        
        redirectAttributes.addFlashAttribute("pointsToExchange", points);
        redirectAttributes.addFlashAttribute("method", method);
        redirectAttributes.addFlashAttribute("message", "Mã OTP đã được gửi qua " + method + " (Hãy kiểm tra Console LOG).");
        
        return "redirect:/point-exchange/verify";
    }

    @GetMapping("/verify")
    public String showVerifyForm(Model model) {
        if (!model.containsAttribute("pointsToExchange")) {
            return "redirect:/point-exchange";
        }
        return "users/verify-otp";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam("otp") String otp,
                            @RequestParam("pointsToExchange") int pointsToExchange,
                            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier = auth.getName();
        
        if (otpService.verifyOtp(identifier, otp)) {
            User user = userService.findByUsername(identifier).orElseThrow();
            int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
            if (currentPoints >= pointsToExchange) {
                user.setLoyaltyPoints(currentPoints - pointsToExchange);
                userRepository.save(user); // Save directly via repository to avoid re-hashing password
                redirectAttributes.addFlashAttribute("success", "Đổi điểm thành công! Đã trừ " + pointsToExchange + " điểm tích lũy.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không đủ điểm để đổi!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không chính xác hoặc đã hết hạn!");
        }
        
        return "redirect:/point-exchange";
    }
}
