package uz.muso.debtbook.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionProjection {
    Long getId();

    String getType(); // DEBT or PAYMENT

    BigDecimal getAmount();

    LocalDate getTransactionDate();

    String getDescription();
}
