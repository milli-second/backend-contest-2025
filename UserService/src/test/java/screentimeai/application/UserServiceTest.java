package screentimeai.application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import screentimeai.application.UserService;
import screentimeai.domain.UserRepository;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;  // 직접 new 하지 않고 스프링에서 주입받음

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    void UserService_success() {
        // given
        userService.signup("hong", "old123");

        // when
        userService.changePassword("hong", "old123", "new123");

        // then
        var user = userRepository.findByNickname("hong").orElseThrow();
        assertTrue(encoder.matches("new123", user.getPassword()));
    }
    @Test
    void UserService_deactivate_success() {
        // given
        userService.signup("kim", "pw123");

        // when
        userService.deactivateAccount("kim");

        // then
        var user = userRepository.findByNickname("kim").orElseThrow();
        assertFalse(user.isActive()); // ✅ 탈퇴된 상태 확인
    }

}
