package uz.muso.debtbook.dto;

import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class PaymentRequest {
    private Long customerId;
    private BigDecimal amount;
}
