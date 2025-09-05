package aivlecloudnative.config.kafka;

import aivlecloudnative.application.UserService;
import aivlecloudnative.domain.BookViewed;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaFunctionConfig {

    @Bean
    public Consumer<BookViewed> bookViewedConsumer(UserService userService) {
        return bookViewed -> {
            System.out.println("📘 Book Viewed: " + bookViewed);
            userService.updateBookRead(bookViewed);
        };
    }

    @Bean
    public Supplier<String> bookViewedProducer() {
        return () -> {
            // 예시: outbound 메시지 발송 로직
            return "{\"type\":\"BookViewed\", \"value\":\"test\"}";
        };
    }
}
