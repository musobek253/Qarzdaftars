package uz.muso.debtbook.service;

import org.springframework.stereotype.Service;

@Service
@Service
public class EmailService {

    @org.springframework.beans.factory.annotation.Value("${spring.mailgun.api-key:}")
    private String apiKey;

    @org.springframework.beans.factory.annotation.Value("${spring.mailgun.domain:}")
    private String domain;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:noreply@debtbook.com}")
    private String fromEmail;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    public void sendCode(String to, String code) {
        if (apiKey == null || apiKey.isEmpty() || domain == null || domain.isEmpty()) {
            System.err.println("❌ Mailgun Config Missing! cannot send email.");
            return;
        }

        String url = "https://api.mailgun.net/v3/" + domain + "/messages";

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBasicAuth("api", apiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> map = new org.springframework.util.LinkedMultiValueMap<>();
        map.add("from", "Debtbook <" + fromEmail + ">");
        map.add("to", to);
        map.add("subject", "Kirish kodi");
        map.add("text", "Do‘kon tizimiga kirish kodi: " + code);

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new org.springframework.http.HttpEntity<>(
                map, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Email sent via Mailgun! To: " + to);
        } catch (Exception ex) {
            System.err.println("❌ Failed to send via Mailgun: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
