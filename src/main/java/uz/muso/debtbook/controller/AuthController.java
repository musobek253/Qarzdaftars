package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.dto.VerifyCodeRequest;
import uz.muso.debtbook.service.AuthService;
import uz.muso.debtbook.service.SmsOtpService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SmsOtpService smsOtpService;
    private final AuthService authService;

    public AuthController(SmsOtpService smsOtpService, AuthService authService) {
        this.smsOtpService = smsOtpService;
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Map<String, String> sendCode(@RequestBody Map<String, String> body) {

        String phoneNumber = body.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new RuntimeException("Telefon raqam majburiy");
        }

        smsOtpService.sendCode(phoneNumber);

        return Map.of(
                "status", "code_sent",
                "message", "Tasdiqlash kodi telefoningizga yuborildi");
    }

    @PostMapping("/verify-code")
    public Map<String, String> verifyCode(@RequestBody VerifyCodeRequest req) {

        if (req.getPhoneNumber() == null || req.getPhoneNumber().isBlank()) {
            throw new RuntimeException("Telefon raqam majburiy");
        }

        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new RuntimeException("Kod majburiy");
        }

        String accessKey = authService.verify(
                req.getPhoneNumber(),
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
