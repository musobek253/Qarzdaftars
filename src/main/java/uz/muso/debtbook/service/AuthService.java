package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.dto.VerifyCodeRequest;
import uz.muso.debtbook.model.Shop;
import uz.muso.debtbook.model.SmsCode;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.ShopRepository;
import uz.muso.debtbook.repository.SmsCodeRepository;
import uz.muso.debtbook.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final SmsCodeRepository codeRepo;
    private final UserRepository userRepo;
    private final ShopRepository shopRepo;

    public AuthService(SmsCodeRepository codeRepo,
            UserRepository userRepo,
            ShopRepository shopRepo) {
        this.codeRepo = codeRepo;
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
    }

    @Transactional
    public String verify(String phoneNumber, String code,
            String shopName, String shopAddress) {

        SmsCode sc = codeRepo
                .findTopByPhoneNumberAndUsedFalseOrderByCreatedAtDesc(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Kod topilmadi"));

        if (sc.isUsed() || sc.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Kod eskirgan");

        if (!sc.getCode().equals(code))
            throw new RuntimeException("Kod noto‘g‘ri");

        sc.setUsed(true);
        codeRepo.save(sc);

        User user = (User) userRepo.findByPhoneNumber(phoneNumber).orElse(null);

        if (user == null) {
            Shop shop = new Shop();
            shop.setName(shopName);
            shop.setAddress(shopAddress);
            shopRepo.save(shop);

            user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setShop(shop);
        }

        user.setAccessKey(UUID.randomUUID().toString());
        userRepo.save(user);

        return user.getAccessKey();
    }

    @Transactional
    public String loginByTelegram(Long chatId) {
        User user = userRepo.findByTelegramChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        if (user.getAccessKey() == null) {
            user.setAccessKey(UUID.randomUUID().toString());
            userRepo.save(user);
        }
        return user.getAccessKey();
    }

}
