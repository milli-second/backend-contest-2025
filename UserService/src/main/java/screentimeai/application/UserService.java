package screentimeai.application;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.*;

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

    public void changePassword(String nickname, String oldPassword, String newPassword) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));

        user.changePassword(oldPassword, newPassword, passwordEncoder);

        userRepository.save(user); // 👉 이벤트 발행됨
        System.out.println("✅ Password changed for user: " + nickname);
    }


    public void deactivateAccount(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nickname));
        user.deactivate();
        userRepository.save(user); // 👉 이벤트 발행 트리거
        System.out.println("🗑️ Account deactivated: " + nickname);
    }


}





