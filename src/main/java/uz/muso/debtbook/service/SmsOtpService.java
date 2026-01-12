package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.SmsCode;
import uz.muso.debtbook.repository.SmsCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SmsOtpService {

    private final SmsCodeRepository repo;
    private final EskizSmsService smsService;

    public SmsOtpService(SmsCodeRepository repo, EskizSmsService smsService) {
        this.repo = repo;
        this.smsService = smsService;
    }

    @Transactional
    public void sendCode(String phoneNumber) {

        repo.deleteByExpiresAtBefore(LocalDateTime.now().minusMinutes(1));

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        SmsCode sc = new SmsCode();
        sc.setPhoneNumber(phoneNumber);
        sc.setCode(code);
        sc.setExpiresAt(LocalDateTime.now().plusMinutes(3));

        repo.save(sc);
        smsService.sendSms(phoneNumber, "Tasdiqlash kodi: " + code);
    }
}
