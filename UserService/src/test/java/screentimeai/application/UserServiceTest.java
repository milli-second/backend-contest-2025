package screentimeai.application;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import screentimeai.UserserviceApplication;
import screentimeai.domain.User;
import screentimeai.domain.UserRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserserviceApplication.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signup_and_login_success_with_kafka_publish() {
        // given
        String nickname = "mammy";
        String rawPassword = "test4444";

        // when (회원가입)
        userService.signup(nickname, rawPassword);

        // then (DB 확인)
        User savedUser = userRepository.findAll().getFirst();
        assertThat(savedUser.getNickname()).isEqualTo(nickname);
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword()))
                .isTrue();

        // then (Kafka 메시지 확인 - signup)
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroupSignup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("user_signup"));

        ConsumerRecords<String, String> signupRecords = consumer.poll(Duration.ofSeconds(5));
        boolean signupFound = false;
        for (ConsumerRecord<String, String> record : signupRecords.records("user_signup")) {
            if (record.value().contains(nickname)) {
                signupFound = true;
                break;
            }
        }
        consumer.close();

        assertThat(signupFound).isTrue();
        System.out.println("✅ Kafka signup message received!");

        // when (로그인)
        boolean loginResult = userService.login(nickname, rawPassword);

        // then (로그인 성공 확인)
        assertThat(loginResult).isTrue();

        // then (Kafka 메시지 확인 - login)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroupLogin"); // 새로운 그룹 ID
        KafkaConsumer<String, String> loginConsumer = new KafkaConsumer<>(props);
        loginConsumer.subscribe(Collections.singletonList("user_login"));

        ConsumerRecords<String, String> loginRecords = loginConsumer.poll(Duration.ofSeconds(5));
        boolean loginFound = false;
        for (ConsumerRecord<String, String> record : loginRecords.records("user_login")) {
            if (record.value().contains("login") && record.value().contains(nickname)) {
                loginFound = true;
                break;
            }
        }
        loginConsumer.close();

        assertThat(loginFound).isTrue();
        System.out.println("✅ Kafka login message received!");
    }
}
