package uz.muso.debtbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// @Service
public class EskizSmsService {

    @Value("${eskiz.email:}")
    private String email;

    @Value("${eskiz.password:}")
    private String password;

    private String token;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://notify.eskiz.uz/api";

    public void sendSms(String phoneNumber, String message) {
        if (token == null) {
            refreshToken();
        }

        try {
            send(phoneNumber, message);
        } catch (Exception e) {
            // If 401, retry once
            System.err.println("⚠️ SMS Send failed, retrying after refresh: " + e.getMessage());
            refreshToken();
            send(phoneNumber, message);
        }
    }

    private void send(String phoneNumber, String message) {
        String url = BASE_URL + "/message/sms/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Clean phone number (remove + if exists, Eskiz usually expects 998xxxxxxxxx)
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");

        org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("mobile_phone", cleanPhone);
        body.add("message", message);
        body.add("from", "4546"); // Standard Eskiz sender

        HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
        System.out.println("✅ SMS sent to " + phoneNumber);
    }

    private void refreshToken() {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new RuntimeException("❌ Eskiz credentials missing in application.yml");
        }

        System.out.println("🔄 Eskiz: Logging in with " + email + " (Pass len: " + password.length() + ")");

        String url = BASE_URL + "/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", password);

        HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            this.token = (String) data.get("token");
            System.out.println("✅ Eskiz Token Refreshed Successfully");
        } catch (Exception e) {
            System.err.println("❌ Eskiz Login Failed: " + e.getMessage());
            throw new RuntimeException("❌ Failed to login to Eskiz: " + e.getMessage());
        }
    }
}
