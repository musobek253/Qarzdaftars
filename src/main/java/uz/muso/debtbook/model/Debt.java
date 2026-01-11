package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDate debtDate;
    private String description;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDate dueDate;          // to‘lov muddati
    @Column(nullable = false)
    private boolean isClosed = false; // qarz yopiq yoki ochiqligini ko'rsatadi

    @Column(nullable = false)
    private boolean isReminderSent = false;

}
