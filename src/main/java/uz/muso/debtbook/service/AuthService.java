package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.dto.VerifyCodeRequest;
import uz.muso.debtbook.model.Shop;
import uz.muso.debtbook.model.EmailCode;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.ShopRepository;
import uz.muso.debtbook.repository.EmailCodeRepository;
import uz.muso.debtbook.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final EmailCodeRepository codeRepo;
    private final UserRepository userRepo;
    private final ShopRepository shopRepo;

    public AuthService(EmailCodeRepository codeRepo,
            UserRepository userRepo,
            ShopRepository shopRepo) {
        this.codeRepo = codeRepo;
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
    }

    @Transactional
    public String verify(String email, String code,
            String shopName, String shopAddress) {

        EmailCode ec = codeRepo
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("Kod topilmadi"));

        if (ec.isUsed() || ec.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Kod eskirgan");

        if (!ec.getCode().equals(code))
            throw new RuntimeException("Kod noto‘g‘ri");

        ec.setUsed(true);
        codeRepo.save(ec);

        User user = (User) userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            Shop shop = new Shop();
            shop.setName(shopName);
            shop.setAddress(shopAddress);
            shopRepo.save(shop);

            user = new User();
            user.setEmail(email);
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
