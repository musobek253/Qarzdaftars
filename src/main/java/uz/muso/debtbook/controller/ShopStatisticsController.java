package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.dto.StatisticsDto;
import uz.muso.debtbook.dto.TransactionDto;
import uz.muso.debtbook.service.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class ShopStatisticsController {

    private final StatisticsService statsService;

    public ShopStatisticsController(StatisticsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public StatisticsDto getStats(@RequestHeader("X-ACCESS-KEY") String accessKey) {
        return statsService.getShopStatistics(accessKey);
    }

    @GetMapping("/transactions")
    public List<TransactionDto> getTransactions(@RequestHeader("X-ACCESS-KEY") String accessKey) {
        return statsService.getShopTransactions(accessKey);
    }
}
