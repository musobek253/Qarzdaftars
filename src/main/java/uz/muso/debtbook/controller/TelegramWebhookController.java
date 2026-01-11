package uz.muso.debtbook.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.muso.debtbook.service.telegram.TelegramUpdateHandler;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final TelegramUpdateHandler handler;

    public TelegramWebhookController(TelegramUpdateHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/webhook")
    public void onUpdate(@RequestBody Map<String, Object> update) {
        System.out.println("DEBUG: Webhook received update: " + update);
        try {
            handler.handle(update);
        } catch (Exception e) {
            System.err.println("ERROR processing update:");
            e.printStackTrace();
            throw e; // rethrow to keep 500 status for now, or swallow if we want 200 OK to telegram
        }
    }
}
