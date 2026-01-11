package uz.muso.debtbook.service.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.ShopRepository;
import uz.muso.debtbook.repository.UserRepository;
import uz.muso.debtbook.service.AuthService;
import uz.muso.debtbook.service.EmailOtpService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TelegramUpdateHandlerTest {

    private TelegramUpdateHandler handler;

    @Mock
    private TelegramService telegramService;
    @Mock
    private EmailOtpService emailOtpService;
    @Mock
    private AuthService authService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private CustomerRepository customerRepo;
    @Mock
    private DebtRepository debtRepo;
    @Mock
    private ShopRepository shopRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new TelegramUpdateHandler(telegramService, emailOtpService, authService, userRepo, customerRepo,
                debtRepo, shopRepo);
    }

    private Map<String, Object> createUpdate(Long chatId, String text) {
        return Map.of(
                "message", Map.of(
                        "chat", Map.of("id", chatId),
                        "text", text));
    }

    @Test
    void testStart() {
        handler.handle(createUpdate(123L, "/start"));
        verify(telegramService).sendMessage(eq(123L), contains("emailingizni kiriting"));
    }

    @Test
    void testLoginFlow_ExistingUser() {
        Long chatId = 123L;
        String email = "test@example.com";
        String code = "123456";

        // Step 1: Start
        handler.handle(createUpdate(chatId, "/start"));

        // Step 2: Enter Email
        handler.handle(createUpdate(chatId, email));
        verify(emailOtpService).sendCode(email);
        verify(telegramService).sendMessage(eq(chatId), contains("Tasdiqlash kodi"));

        // Mock User exists
        User user = new User();
        user.setEmail(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(authService.verify(eq(email), eq(code), isNull(), isNull())).thenReturn("access-key-123");

        // Step 3: Enter Code
        handler.handle(createUpdate(chatId, code));

        verify(authService).verify(eq(email), eq(code), isNull(), isNull());
        verify(telegramService).sendMessage(eq(chatId), contains("Muvaffaqiyatli kirdingiz"));
        verify(userRepo, atLeast(1)).save(user);
    }

    @Test
    void testRegisterFlow_NewUser() {
        Long chatId = 456L;
        String email = "new@example.com";
        String code = "654321";
        String shopName = "My Shop";
        String shopAddress = "Tashkent";

        User newUser = new User();
        newUser.setEmail(email);

        // Robust mocking using AtomicInteger to simulate "exists? No" -> "exists? Yes"
        // sequence
        AtomicInteger callCount = new AtomicInteger(0);
        when(userRepo.findByEmail(eq(email))).thenAnswer(inv -> {
            if (callCount.getAndIncrement() == 0)
                return Optional.empty(); // First call (handleCode)
            return Optional.of(newUser); // Second call (handleShopAddress)
        });

        when(authService.verify(eq(email), eq(code), eq(shopName), eq(shopAddress))).thenReturn("new-access-key");

        // Step 1: Start
        handler.handle(createUpdate(chatId, "/start"));

        // Step 2: Email
        handler.handle(createUpdate(chatId, email));

        // Step 3: Code
        handler.handle(createUpdate(chatId, code));
        verify(telegramService).sendMessage(eq(chatId), contains("Siz yangi foydalanuvchisiz"));
        verify(telegramService).sendMessage(eq(chatId), contains("nomini kiriting"));

        // Step 4: Shop Name
        handler.handle(createUpdate(chatId, shopName));
        verify(telegramService).sendMessage(eq(chatId), contains("manzilini kiriting"));

        // Step 5: Shop Address
        handler.handle(createUpdate(chatId, shopAddress));

        verify(authService).verify(eq(email), eq(code), eq(shopName), eq(shopAddress));
        verify(userRepo).save(newUser);
        verify(telegramService).sendMessage(eq(chatId), contains("Ro'yxatdan o'tish muvaffaqiyatli"));
    }
}
