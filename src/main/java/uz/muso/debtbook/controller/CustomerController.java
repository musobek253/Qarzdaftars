package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.service.CurrentUserService;
import uz.muso.debtbook.service.CustomerBalanceService;
import uz.muso.debtbook.service.CustomerService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepo;
    private final CurrentUserService currentUserService;
    private final CustomerService customerService;
    private final CustomerBalanceService customerBalanceService;

    public CustomerController(CustomerRepository customerRepo,
            CurrentUserService currentUserService,
            CustomerService customerService,
            CustomerBalanceService customerBalanceService) {
        this.customerRepo = customerRepo;
        this.currentUserService = currentUserService;
        this.customerService = customerService;
        this.customerBalanceService = customerBalanceService;
    }

    @PostMapping
    public Customer create(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @RequestBody Customer customer) {
        User user = currentUserService.getByAccessKey(accessKey);
        customer.setShop(user.getShop());
        return customerRepo.save(customer);
    }

    @GetMapping
    public List<uz.muso.debtbook.dto.CustomerDto> list(
            @RequestHeader("X-ACCESS-KEY") String accessKey) {
        User user = currentUserService.getByAccessKey(accessKey);
        return customerRepo.findAllWithBalance(user.getShop().getId());
    }

    @GetMapping("/{id}/total-debt")
    public BigDecimal totalDebt(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id) {
        return customerService.getTotalDebt(accessKey, id);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal balance(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id) {
        return customerBalanceService.remaining(accessKey, id);
    }

    @GetMapping("/{id}/debts")
    public List<uz.muso.debtbook.model.Debt> getDebts(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id) {
        return customerService.getDebts(accessKey, id);
    }

    @GetMapping("/{id}/transactions")
    public List<uz.muso.debtbook.dto.TransactionDto> getTransactions(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id) {
        return customerService.getTransactions(accessKey, id);
    }

    @PostMapping("/{id}/reminder")
    public void sendReminder(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id) {
        customerService.sendDebtReminder(id, accessKey);
    }
}
