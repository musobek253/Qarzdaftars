package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import uz.muso.debtbook.model.SmsCode;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

    Optional<SmsCode> findTopByPhoneNumberAndUsedFalseOrderByCreatedAtDesc(String phoneNumber);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime time);
}
