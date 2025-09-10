package screentimeai.application;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.UserserviceApplication;
import screentimeai.domain.User;
import screentimeai.domain.UserRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserserviceApplication.class)
@Transactional
@Commit  // 테스트 끝날 때 rollback 대신 commit
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearDB() {
        userRepository.deleteAll(); // 매번 테스트 전에 DB 비움
    }

    // ✅ 회원가입 테스트
    @Test
    void signup_success_and_kafka_publish() {
        String nickname = "elong";
        String rawPassword = "pass1234";

        userService.signup(nickname, rawPassword);

        // DB 검증
        User savedUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalStateException("❌ 저장된 유저 없음"));
        assertThat(savedUser.getNickname()).isEqualTo(nickname);
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();

        // Kafka 검증
        boolean signupFound = pollKafkaForMessage("user_signup", nickname);
        assertThat(signupFound).isTrue();
        System.out.println("✅ Kafka signup message received!");
    }

    // ✅ 로그인 테스트
    @Test
    void login_success_and_kafka_publish() {
        String nickname = "jammy";
        String rawPassword = "pass5678";

        // 미리 유저 저장
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        boolean loginResult = userService.login(nickname, rawPassword);

        // 로그인 성공 확인
        assertThat(loginResult).isTrue();

        // Kafka 검증
        boolean loginFound = pollKafkaForMessage("user_login", nickname);
        assertThat(loginFound).isTrue();
        System.out.println("✅ Kafka login message received!");
    }

    // ✅ 로그아웃 테스트
    @Test
    void logout_success_and_kafka_publish() {
        String nickname = "soomin";

        // 유저 미리 저장 (로그아웃은 비번 필요 없음)
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode("dummyPass"));
        userRepository.save(user);

        // 로그아웃 호출
        userService.logout(nickname);

        // Kafka 검증
        boolean logoutFound = pollKafkaForMessage("user_logout", nickname);
        assertThat(logoutFound).isTrue();
        System.out.println("✅ Kafka logout message received!");
    }

    // 🔽 공통 Kafka 확인 메서드
    private boolean pollKafkaForMessage(String topic, String keyword) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup-" + topic + "-" + System.currentTimeMillis());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        boolean found = false;
        for (ConsumerRecord<String, String> record : records.records(topic)) {
            if (record.value().contains(keyword)) {
                found = true;
                break;
            }
        }
        consumer.close();
        return found;
    }
}
