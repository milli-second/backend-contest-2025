package screentimeai.config.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
public class KafkaProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaProcessor.class);

    @Bean
    public Consumer<String> eventConsumer() {
        return message -> {
            logger.info("📨 Kafka 메시지 수신: {}", message);
            System.out.println("📨 UserService에서 Kafka 메시지 수신: " + message);
        };
    }
    
    // StreamBridge를 사용한 수동 메시지 발송으로 대체
}
