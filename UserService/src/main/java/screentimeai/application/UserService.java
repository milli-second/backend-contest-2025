package screentimeai.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.*;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PasswordEncoder passwordEncoder; // ✅ 추가


    public UserService(ApplicationEventPublisher applicationEventPublisher, UserRepository userRepository, KafkaTemplate<String,String> kafkaTemplate, PasswordEncoder passwordEncoder) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
        this.kafkaTemplate=kafkaTemplate;
        this.passwordEncoder = passwordEncoder;

    }

    // ✅ 로그인 (비밀번호 검증 + Kafka 발행 포함)
    public boolean login(String nickname, String rawPassword) {
        System.out.println("🔐 Processing user login: " + nickname);

        User user = userRepository.findAll().stream()
                .filter(u -> u.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));

        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("❌ 비밀번호 불일치");
        }
        LoggedIn loggedInEvent = new LoggedIn();
        loggedInEvent.setId(user.getId());
        loggedInEvent.setNickname(nickname);

        // 🔔 스프링 이벤트 발행
        applicationEventPublisher.publishEvent(loggedInEvent);

        // 🔔 Kafka 발행
        String topic = "user_login";
        String message = String.format("User{id=%d,nickname=%s,action=login}", user.getId(), nickname);
        kafkaTemplate.send(topic, message);

        System.out.println("✅ User login success: " + nickname);
        return true;
    }

    public boolean logout(String nickname) {
        System.out.println("🚪 Processing user logout: " + nickname);

        // 1. 유저 존재 여부 확인
        User user = userRepository.findAll().stream()
                .filter(u -> u.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));

        // 2. 스프링 이벤트 발행
        LoggedOut loggedOutEvent = new LoggedOut();
        loggedOutEvent.setId(user.getId());
        loggedOutEvent.setNickname(nickname);
        applicationEventPublisher.publishEvent(loggedOutEvent);

        // 3. Kafka 발행
        String topic = "user_logout";
        String message = String.format("User{id=%d,nickname=%s,action=logout}", user.getId(), nickname);
        kafkaTemplate.send(topic, message);

        System.out.println("✅ User logout success: " + nickname);
        return true;
    }

    /*public void signup(String nickname,String rawPassword) {
        System.out.println("✍️ Processing user signup: " + nickname);

        //1 닉네임 중복 체크
        if(userRepository.existsByNickname(nickname)){
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: "+nickname);
        }
        //2 User 저장
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(rawPassword)); // ✅ 비밀번호 암호화 저장
        userRepository.save(user);

        //3 이벤트 발행

        SignedUp signedUpEvent = new SignedUp();
        signedUpEvent.setId(user.getId());
        signedUpEvent.setNickname(nickname);

        //카프카 발행
        String topic="user_signup";
        // ✅ 비밀번호도 메시지에 포함
        String message = String.format("User{id=%d,nickname=%s,password=%s}",
                user.getId(), nickname,user.getPassword());//테스트 용이어서 비번 포함, 나중에 뺄 것


        System.out.println("📤 Publishing SignedUp domain event from service");
        applicationEventPublisher.publishEvent(signedUpEvent);
        kafkaTemplate.send(topic, message);//메세지 보내기

        System.out.println("✅ User signup completed: " + nickname);
    }

    // ✅ DB 전체 유저 조회

    public List<User> findAll() {
        return userRepository.findAll();
    }*/

    public void signup(String nickname, String rawPassword) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(rawPassword));
        //user.markSignedUp(); // ✅ 이벤트 기록

        userRepository.save(user); // 👉 여기서 @PostPersist가 자동 실행됨
        user.markSignedUp(); // ✅ 이벤트 기록


        // ✅ 도메인 이벤트 생성
//        SignedUp event = new SignedUp();
//        event.setId(user.getId());
//        event.setNickname(user.getNickname());
//
//        // ✅ Spring 이벤트로 발행
//        applicationEventPublisher.publishEvent(event);
        // 이벤트 발행은 @PostPersist가 대신 해줌.

        System.out.println("📤 User signup event published for nickname=" + user.getNickname());

    }




}