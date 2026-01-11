package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.muso.debtbook.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAccessKey(String accessKey);

    Optional<User> findByEmail(String email);

    Optional<User> findByTelegramChatId(Long telegramChatId);

    Optional<User> findTopByOrderByIdDesc();
}
