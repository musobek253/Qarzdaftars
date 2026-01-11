package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.Payment;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

    @Service
    public class PaymentService {

        private final PaymentRepository paymentRepo;
        private final DebtRepository debtRepo;
        private final CustomerRepository customerRepo;
        private final CurrentUserService currentUserService;

        public PaymentService(PaymentRepository paymentRepo,
                              DebtRepository debtRepo,
                              CustomerRepository customerRepo,
                              CurrentUserService currentUserService) {
            this.paymentRepo = paymentRepo;
            this.debtRepo = debtRepo;
            this.customerRepo = customerRepo;
            this.currentUserService = currentUserService;
        }

        /**
         * CUSTOMER JAMI QARZI BO‘YICHA TO‘LOV
         */
        @Transactional
        public Payment pay(String accessKey, Long customerId, BigDecimal amount) {

            // 1. Summa tekshiruvi
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("To‘lov summasi noto‘g‘ri");
            }

            // 2. User va shop aniqlash
            User user = currentUserService.getByAccessKey(accessKey);

            // 3. Customer tekshirish (faqat o‘z shopi)
            Customer customer = customerRepo
                    .findByIdAndShopId(customerId, user.getShop().getId())
                    .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

            // 4. Customer jami qarzini hisoblash
            BigDecimal totalDebt = debtRepo.findByCustomerId(customerId)
                    .stream()
                    .map(d -> d.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5. Customer jami to‘lovlarini hisoblash
            BigDecimal totalPaid = paymentRepo.totalPaidByCustomer(customerId);

            // 6. Qoldiq
            BigDecimal remaining = totalDebt.subtract(totalPaid);

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Mijozning qarzi yo‘q");
            }

            if (amount.compareTo(remaining) > 0) {
                throw new RuntimeException("To‘lov qarzdan oshib ketdi");
            }

            // 7. Payment saqlash
            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setAmount(amount);
            payment.setPaymentDate(LocalDate.now());

            return paymentRepo.save(payment);
        }
    }

