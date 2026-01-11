package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    private String fullName;
    private String phone;
    private String address;
    @Column
    private Long telegramChatId;


    private LocalDateTime createdAt = LocalDateTime.now();

}