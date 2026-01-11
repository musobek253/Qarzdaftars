package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.EmailCode;
import uz.muso.debtbook.repository.EmailCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailOtpService {

    private final EmailCodeRepository repo;
    private final EmailService emailService;
    private final uz.muso.debtbook.repository.UserRepository userRepo;
    private final uz.muso.debtbook.service.telegram.TelegramService telegramService;

    public EmailOtpService(EmailCodeRepository repo,
            EmailService emailService,
            uz.muso.debtbook.repository.UserRepository userRepo,
            uz.muso.debtbook.service.telegram.TelegramService telegramService) {
        this.repo = repo;
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.telegramService = telegramService;
    }

    @Transactional
    public void sendCode(String email) {

        repo.deleteByExpiresAtBefore(LocalDateTime.now().minusMinutes(1));

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        EmailCode ec = new EmailCode();
        ec.setEmail(email);
        ec.setCode(code);
        ec.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        repo.save(ec);

        // 1. Log to Console (ALWAYS works)
        System.out.println("🔐 SECURITY ALERT: Your Login Code is: " + code);
        System.out.println("👉 (Sending to email: " + email + ")");

        // 2. Try to send via Telegram if user exists
        try {
            var userOpt = userRepo.findByEmail(email);
            if (userOpt.isPresent() && userOpt.get().getTelegramChatId() != null) {
                Long chatId = userOpt.get().getTelegramChatId();
                telegramService.sendMessage(chatId, "🔐 Your Login Code: `" + code + "`");
                System.out.println("✅ Code sent to Telegram Chat ID: " + chatId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send to Telegram: " + e.getMessage());
        }

        // 3. Try Email (Might fail on Railway)
        try {
            emailService.sendCode(email, code);
        } catch (Exception e) {
            System.err.println("❌ Email failed (likely blocked port): " + e.getMessage());
            // Do NOT throw exception, so user can still login using Log/Telegram code
        }
    }
}
