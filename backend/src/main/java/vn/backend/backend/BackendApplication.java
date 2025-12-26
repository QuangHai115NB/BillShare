package vn.backend.backend;

import io.github.cdimascio.dotenv.Dotenv; // Nếu dùng thư viện Dotenv
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        // Nạp .env vào System Properties trước khi Spring chạy
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(BackendApplication.class, args);
    }
}
