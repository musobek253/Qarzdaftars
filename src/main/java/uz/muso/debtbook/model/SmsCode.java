package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sms_codes")
public class SmsCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String code;
    private boolean used = false;
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}
