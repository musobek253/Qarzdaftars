package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDate paymentDate = LocalDate.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}

        // getters / setters


