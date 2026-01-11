package uz.muso.debtbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DebtbookApplication {

    public static void main(String[] args) {
        System.out.println("🔍 STARTUP: Checking Environment Variables...");
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String port = System.getenv("PORT");

        System.out.println("👉 DB_URL: " + (dbUrl != null ? dbUrl : "NULL (Missing!)"));
        System.out.println("👉 DB_USERNAME: " + (dbUser != null ? dbUser : "NULL (Missing!)"));
        System.out.println("👉 PORT: " + (port != null ? port : "NULL (Default 8080)"));

        SpringApplication.run(DebtbookApplication.class, args);
    }

}
