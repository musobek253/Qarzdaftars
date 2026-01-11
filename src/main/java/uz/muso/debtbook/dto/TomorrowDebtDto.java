package uz.muso.debtbook.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class TomorrowDebtDto {

    private Long debtId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;

    public TomorrowDebtDto(Long debtId,
                           Long customerId,
                           String customerName,
                           String customerPhone,
                           BigDecimal remainingAmount,
                           LocalDate dueDate) {
        this.debtId = debtId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.remainingAmount = remainingAmount;
        this.dueDate = dueDate;
    }

    // getters
}
