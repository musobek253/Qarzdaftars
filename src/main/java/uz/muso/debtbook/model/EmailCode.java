package uz.muso.debtbook.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "email_codes")
public class EmailCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String code;
    private boolean used = false;
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}
