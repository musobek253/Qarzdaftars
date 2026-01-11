package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepo;

    public CurrentUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User getByAccessKey(String accessKey) {
        return userRepo.findByAccessKey(accessKey)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
    }
}
