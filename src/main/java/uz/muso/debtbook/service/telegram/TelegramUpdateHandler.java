package uz.muso.debtbook.service.telegram;

import org.springframework.stereotype.Service;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.Debt;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.ShopRepository;
import uz.muso.debtbook.repository.UserRepository;
import uz.muso.debtbook.service.AuthService;
import uz.muso.debtbook.service.EmailOtpService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramUpdateHandler {

    private final TelegramService telegramService;
    private final EmailOtpService emailOtpService;
    private final AuthService authService;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final DebtRepository debtRepo;
    private final ShopRepository shopRepo;

    // Simple in-memory session storage
    private final Map<Long, TelegramSession> sessions = new ConcurrentHashMap<>();

    public TelegramUpdateHandler(TelegramService telegramService,
            EmailOtpService emailOtpService,
            AuthService authService,
            UserRepository userRepo,
            CustomerRepository customerRepo,
            DebtRepository debtRepo,
            ShopRepository shopRepo) {
        this.telegramService = telegramService;
        this.emailOtpService = emailOtpService;
        this.authService = authService;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.debtRepo = debtRepo;
        this.shopRepo = shopRepo;
    }

    public void handle(Map<String, Object> update) {
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (message == null)
            return;

        @SuppressWarnings("unchecked")
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        Long chatId = Long.valueOf(chat.get("id").toString());
        String text = (String) message.get("text");
        if (text == null)
            text = "";

        TelegramSession session = sessions.computeIfAbsent(chatId, k -> {
            TelegramSession s = new TelegramSession();
            s.setChatId(chatId);
            return s;
        });

        if ("/start".equals(text)) {
            session.setState(BotState.WAITING_FOR_EMAIL);
            telegramService.sendMessage(chatId, "Assalomu alaykum! Iltimos, emailingizni kiriting:");
            return;
        }

        switch (session.getState()) {
            case WAITING_FOR_EMAIL -> handleEmail(session, text);
            case WAITING_FOR_CODE -> handleCode(session, text);
            case WAITING_FOR_SHOP_NAME -> handleShopName(session, text);
            case WAITING_FOR_SHOP_ADDRESS -> handleShopAddress(session, text);
            case DASHBOARD -> handleDashboard(session, text);

            // New flows
            case WAITING_FOR_CUSTOMER_NAME -> handleCustomerName(session, text);
            case WAITING_FOR_CUSTOMER_PHONE -> handleCustomerPhone(session, text);

            case WAITING_FOR_DEBT_CUSTOMER_ID -> handleDebtCustomerId(session, text);
            case WAITING_FOR_DEBT_AMOUNT -> handleDebtAmount(session, text);
            case WAITING_FOR_DEBT_DESC -> handleDebtDesc(session, text);

            default -> telegramService.sendMessage(chatId, "Noma'lum buyruq. /start ni bosing.");
        }
    }

    private void handleEmail(TelegramSession session, String email) {
        try {
            emailOtpService.sendCode(email);
            session.setTempEmail(email);
            session.setState(BotState.WAITING_FOR_CODE);
            telegramService.sendMessage(session.getChatId(),
                    "Tasdiqlash kodi emailingizga (" + email + ") yuborildi. Kodni kiriting:");
        } catch (Exception e) {
            telegramService.sendMessage(session.getChatId(), "Xatolik: " + e.getMessage());
        }
    }

    private void handleCode(TelegramSession session, String code) {
        session.setTempCode(code);

        // Check if user exists. Repo returns Optional<User>, so we verify presence.
        boolean userExists = userRepo.findByEmail(session.getTempEmail()).isPresent();

        if (userExists) {
            try {
                // Try logging in (null shop info required for existing users in this logic)
                String accessKey = authService.verify(session.getTempEmail(), code, null, null);

                // Link telegram
                User user = userRepo.findByEmail(session.getTempEmail()).orElseThrow();
                user.setTelegramChatId(session.getChatId());
                userRepo.save(user);

                userRepo.save(user);

                session.setState(BotState.DASHBOARD);
                Map<String, Object> keyboard = createWebAppKeyboard(session.getChatId());
                telegramService.sendMessage(session.getChatId(),
                        "Muvaffaqiyatli kirdingiz! ✅\n\nQuyidagi tugma orqali Mini App ni oching yoki menyudan foydalaning:",
                        keyboard);
            } catch (Exception e) {
                telegramService.sendMessage(session.getChatId(),
                        "Xatolik: " + e.getMessage() + "\nQaytadan urinib ko'ring yoki /start ni bosing.");
            }
        } else {
            // New user: ask for Shop Name
            session.setState(BotState.WAITING_FOR_SHOP_NAME);
            telegramService.sendMessage(session.getChatId(),
                    "Siz yangi foydalanuvchisiz.\nIltimos, do'koningiz nomini kiriting:");
        }
    }

    private void handleShopName(TelegramSession session, String shopName) {
        session.setTempShopName(shopName);
        session.setState(BotState.WAITING_FOR_SHOP_ADDRESS);
        telegramService.sendMessage(session.getChatId(), "Do'kon manzilini kiriting:");
    }

    private void handleShopAddress(TelegramSession session, String shopAddress) {
        try {
            // Now verify and register
            String accessKey = authService.verify(
                    session.getTempEmail(),
                    session.getTempCode(),
                    session.getTempShopName(),
                    shopAddress);

            // Link telegram
            User user = userRepo.findByEmail(session.getTempEmail()).orElseThrow();
            user.setTelegramChatId(session.getChatId());
            userRepo.save(user);

            session.setState(BotState.DASHBOARD);

            Map<String, Object> keyboard = createWebAppKeyboard(session.getChatId());
            telegramService.sendMessage(session.getChatId(),
                    "Ro'yxatdan o'tish muvaffaqiyatli! ✅\n\nQuyidagi tugma orqali Mini App ni oching yoki menyudan foydalaning:",
                    keyboard);

        } catch (Exception e) {
            telegramService.sendMessage(session.getChatId(),
                    "Xatolik: " + e.getMessage() + "\n/start ni bosib qaytadan urinib ko'ring.");
            // Reset to start on bad code/error, so user starts fresh
            session.setState(BotState.WAITING_FOR_EMAIL); // Or restart
        }
    }

    private void handleDashboard(TelegramSession session, String text) {
        if ("/mijozlar".equals(text)) {
            handleListCustomers(session);
        } else if ("/mijoz_qoshish".equals(text)) {
            session.setState(BotState.WAITING_FOR_CUSTOMER_NAME);
            telegramService.sendMessage(session.getChatId(), "Yangi mijozning ismini kiriting:");
        } else if ("/qarz_yozish".equals(text)) {
            handleAddDebtStart(session);
        } else if ("/id".equals(text)) {
            telegramService.sendMessage(session.getChatId(), "Sizning ID raqamingiz: `" + session.getChatId() + "`",
                    null);
        } else {
            Map<String, Object> keyboard = createWebAppKeyboard(session.getChatId());
            telegramService.sendMessage(session.getChatId(),
                    "Menyu:\n/mijoz_qoshish - Yangi mijoz qo'shish\n/mijozlar - Mijozlar ro'yxati\n/qarz_yozish - Qarz yozish\n/tolov - To'lov qilish",
                    keyboard);
        }
    }

    private Map<String, Object> createWebAppKeyboard(Long chatId) {
        String url = "https://leticia-stenosed-restrainingly.ngrok-free.dev?chat_id=" + chatId;
        Map<String, Object> webAppInfo = Map.of("url", url);
        Map<String, Object> button = Map.of("text", "📱 Mini App ochish", "web_app", webAppInfo);

        // Using ReplyKeyboardMarkup to show persistent button
        return Map.of(
                "keyboard", List.of(List.of(button)),
                "resize_keyboard", true);
    }

    // --- Customer Flow ---
    private void handleCustomerName(TelegramSession session, String name) {
        session.setTempCustomerName(name);
        session.setState(BotState.WAITING_FOR_CUSTOMER_PHONE);
        telegramService.sendMessage(session.getChatId(),
                "Mijozning telefon raqamini kiriting (masalan: +998901234567):");
    }

    private void handleCustomerPhone(TelegramSession session, String phone) {
        try {
            User user = getUserByChatId(session.getChatId());
            if (user == null || user.getShop() == null) {
                telegramService.sendMessage(session.getChatId(), "Xatolik: Do'kon topilmadi. /start ni bosing.");
                return;
            }

            Customer customer = new Customer();
            customer.setFullName(session.getTempCustomerName());
            customer.setPhone(phone);
            customer.setShop(user.getShop());
            customer.setTelegramChatId(null); // Optional

            customerRepo.save(customer);

            session.setState(BotState.DASHBOARD);
            telegramService.sendMessage(session.getChatId(),
                    "Mijoz muvaffaqiyatli qo'shildi! ✅\nIsm: " + customer.getFullName());

        } catch (Exception e) {
            telegramService.sendMessage(session.getChatId(), "Xatolik: " + e.getMessage());
            session.setState(BotState.DASHBOARD);
        }
    }

    // --- Debt Flow ---
    private void handleAddDebtStart(TelegramSession session) {
        User user = getUserByChatId(session.getChatId());
        if (user == null || user.getShop() == null) {
            telegramService.sendMessage(session.getChatId(), "Xatolik: Do'kon topilmadi.");
            return;
        }

        List<Customer> customers = customerRepo.findByShopId(user.getShop().getId());
        if (customers.isEmpty()) {
            telegramService.sendMessage(session.getChatId(),
                    "Sizda hali mijozlar yo'q. Avval /mijoz_qoshish ni bosing.");
            return;
        }

        StringBuilder sb = new StringBuilder("Qarz yozish uchun mijoz ID raqamini tanlang:\n\n");
        for (Customer c : customers) {
            sb.append("ID: ").append(c.getId()).append(" - ").append(c.getFullName()).append("\n");
        }
        sb.append("\nIltimos, ID raqamni yozing:");

        session.setState(BotState.WAITING_FOR_DEBT_CUSTOMER_ID);
        telegramService.sendMessage(session.getChatId(), sb.toString());
    }

    private void handleDebtCustomerId(TelegramSession session, String text) {
        try {
            Long customerId = Long.parseLong(text);
            // Verify customer belongs to this shop
            User user = getUserByChatId(session.getChatId());
            Customer customer = customerRepo.findById(customerId).orElse(null);

            if (customer == null || !customer.getShop().getId().equals(user.getShop().getId())) {
                telegramService.sendMessage(session.getChatId(),
                        "Mijoz topilmadi yoki xato ID. Qaytadan kiriting (yoki /start):");
                return;
            }

            session.setTempDebtCustomerId(customerId);
            session.setState(BotState.WAITING_FOR_DEBT_AMOUNT);
            telegramService.sendMessage(session.getChatId(), "Qarz miqdorini kiriting (so'mda):");

        } catch (NumberFormatException e) {
            telegramService.sendMessage(session.getChatId(), "Iltimos, faqat raqam kiriting (ID):");
        }
    }

    private void handleDebtAmount(TelegramSession session, String text) {
        try {
            BigDecimal amount = new BigDecimal(text);
            session.setTempDebtAmount(amount);
            session.setState(BotState.WAITING_FOR_DEBT_DESC);
            telegramService.sendMessage(session.getChatId(), "Qarz izohini yozing (masalan: non, sut, ...):");
        } catch (NumberFormatException e) {
            telegramService.sendMessage(session.getChatId(), "Iltimos, to'g'ri summa kiriting (masalan: 10000):");
        }
    }

    private void handleDebtDesc(TelegramSession session, String desc) {
        try {
            User user = getUserByChatId(session.getChatId());
            Customer customer = customerRepo.findById(session.getTempDebtCustomerId()).orElseThrow();

            Debt debt = new Debt();
            debt.setCustomer(customer);
            debt.setShop(user.getShop());
            debt.setTotalAmount(session.getTempDebtAmount());
            debt.setDescription(desc);
            debt.setDebtDate(LocalDate.now());
            debt.setDueDate(LocalDate.now().plusDays(30)); // Default 30 days
            debt.setClosed(false);

            debtRepo.save(debt);

            session.setState(BotState.DASHBOARD);
            telegramService.sendMessage(session.getChatId(),
                    "Qarz yozildi! ✅\nMijoz: " + customer.getFullName() +
                            "\nSumma: " + session.getTempDebtAmount() +
                            "\nIzoh: " + desc);

        } catch (Exception e) {
            telegramService.sendMessage(session.getChatId(), "Xatolik: " + e.getMessage());
            session.setState(BotState.DASHBOARD);
        }
    }

    private void handleListCustomers(TelegramSession session) {
        User user = getUserByChatId(session.getChatId());
        if (user == null) {
            telegramService.sendMessage(session.getChatId(), "Siz tizimga kirmagansiz.");
            return;
        }

        if (user.getShop() == null) {
            telegramService.sendMessage(session.getChatId(), "Do'kon ma'lumotlari mavjud emas.");
            return;
        }

        List<Customer> customers = customerRepo.findByShopId(user.getShop().getId());
        if (customers.isEmpty()) {
            telegramService.sendMessage(session.getChatId(), "Mijozlar yo'q.");
        } else {
            StringBuilder sb = new StringBuilder("Mijozlar:\n");
            for (Customer c : customers) {
                sb.append("ID: ").append(c.getId()).append(" - ").append(c.getFullName()).append(" (")
                        .append(c.getPhone()).append(")\n");
            }
            telegramService.sendMessage(session.getChatId(), sb.toString());
        }
    }

    private User getUserByChatId(Long chatId) {
        return userRepo.findByTelegramChatId(chatId).orElse(null);
    }
}
