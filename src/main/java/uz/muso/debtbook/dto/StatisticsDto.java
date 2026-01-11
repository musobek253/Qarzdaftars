package uz.muso.debtbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDto {
    private BigDecimal totalDebt; // Jami qarz (berilgan)
    private BigDecimal totalPaid; // Jami to'lov (qaytarilgan)
    private BigDecimal balance; // Farq
}
