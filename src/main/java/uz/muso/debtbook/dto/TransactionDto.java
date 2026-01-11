package uz.muso.debtbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto implements Comparable<TransactionDto> {
    private Long id;
    private String type; // "DEBT" or "PAYMENT"
    private BigDecimal amount;
    private LocalDate date;
    private String description;

    @Override
    public int compareTo(TransactionDto o) {
        if (this.date == null && o.date == null)
            return 0;
        if (this.date == null)
            return 1; // Nulls last
        if (o.date == null)
            return -1;
        // Sort by date DESC (newest first)
        return o.date.compareTo(this.date);
    }
}
