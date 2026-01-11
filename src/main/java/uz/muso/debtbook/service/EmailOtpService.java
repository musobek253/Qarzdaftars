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

    public EmailOtpService(EmailCodeRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
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
        emailService.sendCode(email, code);
    }
}


