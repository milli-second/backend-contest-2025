package screentimeai.application;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.*;


/*public class UserService {
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

        User user = userRepository.findAll().stream()
                .filter(u -> u.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));

        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("❌ 비밀번호 불일치");
        }

        user.markLoggedIn(); // ✅ 도메인 이벤트 추가
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

        user.markLoggedOut(); // ✅ 도메인 이벤트 추가
        System.out.println("✅ User logout success: " + nickname);
        return true;
    }

    public void signup(String nickname, String rawPassword) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        User user = new User();
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user); // 👉 여기서 @PostPersist가 자동 실행됨
        user.markSignedUp(); // ✅ 이벤트 기록

        System.out.println("📤 User signup event published for nickname=" + user.getNickname());
    }*/
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(String nickname, String rawPassword) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + nickname);
        }
        User user = new User();
        user.setNickname(nickname);
        user.signup(rawPassword, passwordEncoder);

        userRepository.save(user); // 👉 save + flush에서 DomainEvent 발행
        System.out.println("✅ User signup completed: " + nickname);
    }

    public User login(String nickname, String rawPassword) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));
        user.login(rawPassword, passwordEncoder);

        userRepository.save(user); // 👉 이벤트 발행을 트리거
        if (user.getId() != null) {
            user.markSignedUp();
        }
        System.out.println("✅ User login completed: " + nickname);
        return user;
    }

    public void logout(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));
        user.logout();

        userRepository.save(user); // 👉 이벤트 발행을 트리거
        System.out.println("✅ User logout completed: " + nickname);
    }
}





