package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.EmailCode;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

    Optional<EmailCode> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime time);
}
