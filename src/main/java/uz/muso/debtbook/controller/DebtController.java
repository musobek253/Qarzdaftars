package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.model.Customer;
import uz.muso.debtbook.model.Debt;
import uz.muso.debtbook.model.User;
import uz.muso.debtbook.repository.CustomerRepository;
import uz.muso.debtbook.repository.DebtRepository;
import uz.muso.debtbook.service.CurrentUserService;
import uz.muso.debtbook.service.DebtService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @PostMapping
    public Debt create(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @RequestBody Debt debt
    ) {
        return debtService.create(accessKey, debt);
    }

    @GetMapping
    public List<Debt> list(
            @RequestHeader("X-ACCESS-KEY") String accessKey
    ) {
        return debtService.list(accessKey);
    }

    @PutMapping("/{id}")
    public Debt update(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id,
            @RequestBody Debt debt
    ) {
        return debtService.update(accessKey, id, debt);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @PathVariable Long id
    ) {
        debtService.delete(accessKey, id);
    }
}