package aivlecloudnative.config.kafka;

import aivlecloudnative.domain.User;
import aivlecloudnative.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@example.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123")); // 암호화
                admin.setIsAdmin(true);
                admin.setIsAuthor(true);

                userRepository.save(admin);
                System.out.println("✅ 관리자 계정(admin@example.com) 생성됨");
            }
        };
    }
}

