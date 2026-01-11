package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.Debt;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.PaymentRepository;

import java.math.BigDecimal;

@Service
public class CustomerBalanceService {

    private final DebtRepository debtRepo;
    private final PaymentRepository paymentRepo;
    private final CustomerRepository customerRepo;
    private final CurrentUserService currentUserService;

    public CustomerBalanceService(DebtRepository debtRepo,
                                  PaymentRepository paymentRepo,
                                  CustomerRepository customerRepo,
                                  CurrentUserService currentUserService) {
        this.debtRepo = debtRepo;
        this.paymentRepo = paymentRepo;
        this.customerRepo = customerRepo;
        this.currentUserService = currentUserService;
    }

    public BigDecimal remaining(String accessKey, Long customerId) {

        User user = currentUserService.getByAccessKey(accessKey);

        Customer customer = customerRepo
                .findByIdAndShopId(customerId, user.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        BigDecimal totalDebt = debtRepo.findByCustomerId(customerId)
                .stream()
                .map(Debt::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = paymentRepo.totalPaidByCustomer(customerId);

        return totalDebt.subtract(totalPaid);
    }
}
