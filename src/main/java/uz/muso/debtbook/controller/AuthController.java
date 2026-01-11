package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.dto.VerifyCodeRequest;
import uz.muso.debtbook.service.AuthService;
import uz.muso.debtbook.service.EmailOtpService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmailOtpService emailOtpService;
    private final AuthService authService;

    public AuthController(EmailOtpService emailOtpService, AuthService authService) {
        this.emailOtpService = emailOtpService;
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Map<String, String> sendCode(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email majburiy");
        }

        emailOtpService.sendCode(email);

        return Map.of(
                "status", "code_sent",
                "message", "Tasdiqlash kodi emailingizga yuborildi");
    }

    @PostMapping("/verify-code")
    public Map<String, String> verifyCode(@RequestBody VerifyCodeRequest req) {

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new RuntimeException("Email majburiy");
        }

        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new RuntimeException("Kod majburiy");
        }

        String accessKey = authService.verify(
                req.getEmail(),
                req.getCode(),
                req.getShopName(),
                req.getShopAddress());

        return Map.of(
                "status", "success",
                "accessKey", accessKey);
    }

    @PostMapping("/telegram-login")
    public Map<String, String> telegramLogin(@RequestBody Map<String, Long> body) {
        Long chatId = body.get("chatId");
        if (chatId == null) {
            throw new RuntimeException("chatId majburiy");
        }

        String accessKey = authService.loginByTelegram(chatId);

        return Map.of(
                "status", "success",
                "accessKey", accessKey);
    }

}
