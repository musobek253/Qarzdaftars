package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import uz.muso.debtbook.dto.StatisticsDto;
import uz.muso.debtbook.dto.TransactionDto;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final DebtRepository debtRepo;
    private final PaymentRepository paymentRepo;
    private final CurrentUserService currentUserService;

    public StatisticsService(DebtRepository debtRepo, PaymentRepository paymentRepo,
            CurrentUserService currentUserService) {
        this.debtRepo = debtRepo;
        this.paymentRepo = paymentRepo;
        this.currentUserService = currentUserService;
    }

    public StatisticsDto getShopStatistics(String accessKey) {
        User user = currentUserService.getByAccessKey(accessKey);
        Long shopId = user.getShop().getId();

        BigDecimal totalDebt = debtRepo.sumTotalAmountByShopId(shopId);
        BigDecimal totalPaid = paymentRepo.sumAmountByShopId(shopId);

        if (totalDebt == null)
            totalDebt = BigDecimal.ZERO;
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;

        return new StatisticsDto(totalDebt, totalPaid, totalDebt.subtract(totalPaid));
    }

    public List<TransactionDto> getShopTransactions(String accessKey) {
        User user = currentUserService.getByAccessKey(accessKey);
        Long shopId = user.getShop().getId();

        // Optimized Fetch
        List<uz.muso.debtbook.dto.TransactionProjection> projections = debtRepo.findAllShopTransactions(shopId);

        List<TransactionDto> transactions = new ArrayList<>();
        for (uz.muso.debtbook.dto.TransactionProjection p : projections) {
            transactions.add(new TransactionDto(
                    p.getId(),
                    p.getType(),
                    p.getAmount(),
                    p.getTransactionDate(),
                    p.getDescription()));
        }
        return transactions;
    }
}
