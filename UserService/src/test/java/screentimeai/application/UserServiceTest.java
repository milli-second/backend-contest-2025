package screentimeai.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.User;
import screentimeai.domain.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 테스트 끝나면 DB 롤백
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void signup_success() {
        // given
        String nickname = "jinju";

        // when
        userService.signup(nickname);

        // then
        User savedUser = userRepository.findAll().get(0);
        assertThat(savedUser.getNickname()).isEqualTo(nickname);
        System.out.println("✅ Signup test success: " + savedUser.getNickname());
    }

    @Test
    void signup_fail_when_nickname_already_exists() {
        // given
        String nickname = "jinju";
        userService.signup(nickname);

        // when & then
        try {
            userService.signup(nickname);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("이미 사용 중인 닉네임");
            System.out.println("✅ Duplicate nickname exception confirmed");
        }
    }
}
