package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(unique = true)
    private String accessKey;

    private Long telegramChatId;

    private LocalDateTime createdAt = LocalDateTime.now();

}
