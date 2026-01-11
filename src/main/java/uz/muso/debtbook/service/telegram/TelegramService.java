package uz.muso.debtbook.service.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {

    @Value("${spring.telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(Long chatId, String text) {

        if (chatId == null)
            return;

        String url = "https://api.telegram.org/bot"
                + botToken
                + "/sendMessage";

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);

        restTemplate.postForObject(url, body, String.class);
    }

    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        if (chatId == null)
            return;

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }

        restTemplate.postForObject(url, body, String.class);
    }
}
