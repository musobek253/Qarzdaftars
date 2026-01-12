package uz.muso.debtbook.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String sendGridApiKey;
    private final String fromEmail;

    public EmailService(
            @org.springframework.beans.factory.annotation.Value("${spring.sendgrid.api-key:}") String sendGridApiKey,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.username:noreply@debtbook.com}") String fromEmail) {
        this.sendGridApiKey = sendGridApiKey;
        this.fromEmail = fromEmail;
    }

    public void sendCode(String to, String code) {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            System.err.println("❌ SendGrid API Key is missing! Cannot send email.");
            return;
        }

        com.sendgrid.helpers.mail.objects.Email from = new com.sendgrid.helpers.mail.objects.Email(fromEmail);
        String subject = "Kirish kodi";
        com.sendgrid.helpers.mail.objects.Email toEmail = new com.sendgrid.helpers.mail.objects.Email(to);
        com.sendgrid.helpers.mail.objects.Content content = new com.sendgrid.helpers.mail.objects.Content("text/plain",
                "Do‘kon tizimiga kirish kodi: " + code);
        com.sendgrid.helpers.mail.Mail mail = new com.sendgrid.helpers.mail.Mail(from, subject, toEmail, content);

        com.sendgrid.SendGrid sg = new com.sendgrid.SendGrid(sendGridApiKey);
        com.sendgrid.Request request = new com.sendgrid.Request();
        try {
            request.setMethod(com.sendgrid.Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            com.sendgrid.Response response = sg.api(request);
            System.out.println("✅ Email sent via SendGrid! Status: " + response.getStatusCode());
        } catch (java.io.IOException ex) {
            System.err.println("❌ Failed to send email via SendGrid: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
