package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.PaymentRepository;
import uz.muso.debtbook.service.telegram.TelegramService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final DebtRepository debtRepo;
    // private final PaymentRepository paymentRepo; // Removed as unused
    private final CurrentUserService currentUserService;
    private final TelegramService telegramService;

    public CustomerService(CustomerRepository customerRepo,
            DebtRepository debtRepo,
            PaymentRepository paymentRepo,
            CurrentUserService currentUserService,
            TelegramService telegramService) {
        this.customerRepo = customerRepo;
        this.debtRepo = debtRepo;
        // this.paymentRepo = paymentRepo;
        this.currentUserService = currentUserService;
        this.telegramService = telegramService;
    }

    public BigDecimal getTotalDebt(String accessKey, Long customerId) {

        User user = currentUserService.getByAccessKey(accessKey);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        if (!customer.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        return debtRepo.totalDebtByCustomer(customerId, user.getShop().getId());
    }

    public java.util.List<uz.muso.debtbook.model.Debt> getDebts(String accessKey, Long customerId) {
        User user = currentUserService.getByAccessKey(accessKey);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        if (!customer.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        return debtRepo.findByCustomerIdOrderByDebtDateAsc(customerId);
    }

    public List<uz.muso.debtbook.dto.TransactionDto> getTransactions(String accessKey, Long customerId) {
        User user = currentUserService.getByAccessKey(accessKey);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        if (!customer.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        // Optimized fetching
        List<uz.muso.debtbook.dto.TransactionProjection> projections = debtRepo.findAllTransactions(customerId);

        // Map projection to DTO (if needed, or just return list of projections if
        // controller supports it)
        // Controller returns List<TransactionDto>, so let's map.
        List<uz.muso.debtbook.dto.TransactionDto> dtos = new ArrayList<>();
        for (uz.muso.debtbook.dto.TransactionProjection p : projections) {
            dtos.add(new uz.muso.debtbook.dto.TransactionDto(
                    p.getId(),
                    p.getType(),
                    p.getAmount(),
                    p.getTransactionDate(),
                    p.getDescription()));
        }
        return dtos;
    }

    public void sendDebtReminder(Long customerId, String accessKey) {
        User user = currentUserService.getByAccessKey(accessKey);
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        if (!customer.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo'q");
        }

        if (customer.getTelegramChatId() == null) {
            throw new RuntimeException("Mijozning Telegram hisobi ulanmagan");
        }

        BigDecimal totalDebt = debtRepo.totalDebtByCustomer(customerId, user.getShop().getId());
        if (totalDebt == null)
            totalDebt = BigDecimal.ZERO;

        if (totalDebt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Mijozning qarzi yo'q");
        }

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("uz", "UZ"));
        String amountStr = nf.format(totalDebt);

        String message = String.format("""
                Hurmatli mijoz %s!

                Sizning %s do'konidan %s so'm qarzingiz mavjud.
                Iltimos, to'lovni amalga oshirishingizni so'raymiz.

                Hurmat bilan,
                %s do'koni ma'muriyati.
                """, customer.getFullName(), customer.getShop().getName(), amountStr, customer.getShop().getName());

        telegramService.sendMessage(customer.getTelegramChatId(), message);
    }
}
