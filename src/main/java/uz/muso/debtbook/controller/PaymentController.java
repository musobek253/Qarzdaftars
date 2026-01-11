package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.*;
import uz.muso.debtbook.dto.PaymentRequest;
import uz.muso.debtbook.model.Payment;
import uz.muso.debtbook.service.PaymentService;



@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment pay(
            @RequestHeader("X-ACCESS-KEY") String accessKey,
            @RequestBody PaymentRequest req
    ) {
        return paymentService.pay(
                accessKey,
                req.getCustomerId(),
                req.getAmount()
        );
    }
}

