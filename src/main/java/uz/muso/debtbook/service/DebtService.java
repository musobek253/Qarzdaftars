package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.Debt;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class DebtService {

    private final DebtRepository debtRepo;
    private final CustomerRepository customerRepo;
    private final CurrentUserService currentUserService;

    public DebtService(DebtRepository debtRepo,
                       CustomerRepository customerRepo,
                       CurrentUserService currentUserService) {
        this.debtRepo = debtRepo;
        this.customerRepo = customerRepo;
        this.currentUserService = currentUserService;
    }

    /* CREATE */
    @Transactional
    public Debt create(String accessKey, Debt debt) {

        return getDebt(accessKey, debt, currentUserService, customerRepo, debtRepo);
    }

    public static Debt getDebt(String accessKey, Debt debt, CurrentUserService currentUserService, CustomerRepository customerRepo, DebtRepository debtRepo) {
        User user = currentUserService.getByAccessKey(accessKey);

        Customer customer = customerRepo.findById(debt.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi"));

        if (!customer.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        debt.setShop(user.getShop());
        debt.setCustomer(customer);

        if (debt.getDebtDate() == null) {
            debt.setDebtDate(LocalDate.now());
        }

        return debtRepo.save(debt);
    }

    /* READ (list) */
    public List<Debt> list(String accessKey) {
        User user = currentUserService.getByAccessKey(accessKey);
        return debtRepo.findByShopId(user.getShop().getId());
    }

    /* UPDATE */
    @Transactional
    public Debt update(String accessKey, Long debtId, Debt updated) {

        User user = currentUserService.getByAccessKey(accessKey);

        Debt debt = debtRepo.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Qarz topilmadi"));

        if (!debt.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        debt.setDescription(updated.getDescription());
        debt.setTotalAmount(updated.getTotalAmount());
        debt.setDebtDate(updated.getDebtDate());

        return debt;
    }

    /* DELETE */
    @Transactional
    public void delete(String accessKey, Long debtId) {

        User user = currentUserService.getByAccessKey(accessKey);

        Debt debt = debtRepo.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Qarz topilmadi"));

        if (!debt.getShop().getId().equals(user.getShop().getId())) {
            throw new RuntimeException("Ruxsat yo‘q");
        }

        debtRepo.delete(debt);
    }
}
