package uz.muso.debtbook.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomerDto {
    private Long id;
    private String fullName;
    private String phone;
    private BigDecimal balance;
    private java.time.LocalDate earliestDueDate;

    public CustomerDto(Long id, String fullName, String phone, BigDecimal totalDebt, BigDecimal totalPaid,
            java.time.LocalDate earliestDueDate) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        BigDecimal debt = totalDebt == null ? BigDecimal.ZERO : totalDebt;
        BigDecimal paid = totalPaid == null ? BigDecimal.ZERO : totalPaid;
        this.balance = debt.subtract(paid);
        this.earliestDueDate = earliestDueDate;
    }
}
