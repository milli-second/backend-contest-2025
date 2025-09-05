package screentimeai.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.*;

@Service
@Transactional
public class UserService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;


    public UserService(ApplicationEventPublisher applicationEventPublisher, UserRepository userRepository, KafkaTemplate<String,String> kafkaTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
        this.kafkaTemplate=kafkaTemplate;
    }

    public void login(String nickname) {
        System.out.println("🔐 Processing user login: " + nickname);
        
        // 실제 로그인 비즈니스 로직 (예: 사용자 검증, 세션 생성 등)
        // 현재는 간단하게 이벤트만 발행
        
        LoggedIn loggedInEvent = new LoggedIn();
        loggedInEvent.setId(1L);
        loggedInEvent.setNickname(nickname);
        
        System.out.println("📤 Publishing LoggedIn domain event from service");
        applicationEventPublisher.publishEvent(loggedInEvent);
        
        System.out.println("✅ User login completed: " + nickname);
    }

    public void logout(String nickname) {
        System.out.println("🚪 Processing user logout: " + nickname);
        
        LoggedOut loggedOutEvent = new LoggedOut();
        loggedOutEvent.setId(1L);
        loggedOutEvent.setNickname(nickname);
        
        System.out.println("📤 Publishing LoggedOut domain event from service");
        applicationEventPublisher.publishEvent(loggedOutEvent);
        
        System.out.println("✅ User logout completed: " + nickname);
    }

    public void signup(String nickname) {
        System.out.println("✍️ Processing user signup: " + nickname);

        //1 닉네임 중복 체크
        if(userRepository.existsByNickname(nickname)){
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: "+nickname);
        }
        //2 User 저장
        User user = new User();
        user.setNickname(nickname);
        userRepository.save(user);

        //3 이벤트 발행

        SignedUp signedUpEvent = new SignedUp();
        signedUpEvent.setId(1L);
        signedUpEvent.setNickname(nickname);

        //카프카 발행
        String topic="user_signup";
        String message=String.format("User{id=%d,nickname=%s}",user.getId(),nickname);
        
        System.out.println("📤 Publishing SignedUp domain event from service");
        applicationEventPublisher.publishEvent(signedUpEvent);
        kafkaTemplate.send(topic, message);//메세지 보내기

        System.out.println("✅ User signup completed: " + nickname);
    }
}