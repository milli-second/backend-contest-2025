package aivlecloudnative.signUp;

import aivlecloudnative.application.TokenBlacklistService;
import aivlecloudnative.application.UserService;
import aivlecloudnative.domain.*;
import aivlecloudnative.infra.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// 생략된 import 문은 그대로 유지

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                outboxMessageRepository,
                objectMapper,
                passwordEncoder,
                jwtTokenProvider,
                tokenBlacklistService
        );
    }

    @Test
    @DisplayName("회원가입 시 이메일 중복이면 실패")
    void signUp_shouldThrow_ifEmailExists() {
        SignUpCommand cmd = new SignUpCommand();
        cmd.setEmail("test@example.com");

        when(userRepository.existsByEmail(cmd.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(cmd));
    }

    @Test
    @DisplayName("회원가입 성공 시 비밀번호 암호화 및 Outbox 저장")
    void signUp_shouldEncodePassword_andSaveOutbox() {
        SignUpCommand cmd = new SignUpCommand();
        cmd.setEmail("user@example.com");
        cmd.setUserName("홍길동");
        cmd.setPassword("plain1234");
        cmd.setIsKt(true);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(cmd.getPassword())).thenReturn("encoded1234");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        SignUpResponse response = userService.signUp(cmd);

        Assertions.assertEquals("user@example.com", response.email());
        Assertions.assertEquals("홍길동", response.username());
        Assertions.assertEquals(1L, response.userId());

        verify(outboxMessageRepository).save(any(OutboxMessage.class));
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 포함 LoginResponse 반환")
    void login_shouldReturnToken_whenCredentialsMatch() {
        String rawPassword = "plain1234";
        String encodedPassword = "encoded1234";
        String fakeToken = "jwt.token.string";

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword(encodedPassword);

        LoginCommand cmd = new LoginCommand();
        cmd.setEmail("user@example.com");
        cmd.setPassword(rawPassword);

        when(userRepository.findByEmail(cmd.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.createToken(eq(user.getId()), eq(user.getEmail()), eq(false), eq(false)))
                .thenReturn(fakeToken);

        LoginResponse response = userService.login(cmd);

        Assertions.assertEquals(fakeToken, response.accessToken());
        Assertions.assertEquals("Bearer", response.tokenType());

        verify(jwtTokenProvider).createToken(eq(user.getId()), eq(user.getEmail()), eq(false), eq(false));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음")
    void login_shouldThrow_ifUserNotFound() {
        LoginCommand cmd = new LoginCommand();
        cmd.setEmail("missing@example.com");
        cmd.setPassword("1234");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.login(cmd));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_shouldThrow_ifPasswordIncorrect() {
        LoginCommand cmd = new LoginCommand();
        cmd.setEmail("user@example.com");
        cmd.setPassword("wrongpass");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedpass");

        when(userRepository.findByEmail(cmd.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(cmd.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.login(cmd));
    }

    @Test
    @DisplayName("로그아웃 시 토큰이 블랙리스트에 남은 만료시간으로 등록된다")
    void logout_shouldBlacklistToken_withRemainingExpiration() {
        String token = "jwt.token.string";
        long remainMs = 30 * 60 * 1000L;

        when(jwtTokenProvider.getExpiration(token)).thenReturn(remainMs);

        userService.logout(token);

        verify(tokenBlacklistService).blacklist(token, remainMs);
    }

    @Test
    @DisplayName("KT 유저 열람 신청 시 이벤트 저장")
    void requestContentAccess_shouldSaveKtEvent() {
        User user = new User();
        user.setId(1L);
        user.setIsKt(true);
        user.setHasActiveSubscription(true);

        RequestContentAccessCommand cmd = new RequestContentAccessCommand();
        cmd.setUserId(1L);
        cmd.setBookId(100L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.requestContentAccess(cmd);

        verify(outboxMessageRepository).save(argThat(msg -> msg.getEventType().equals("AccessRequestedAsSubscriber")));
    }

    @Test
    @DisplayName("비 KT 유저 열람 신청 시 이벤트 저장")
    void requestContentAccess_shouldSavePointEvent() {
        User user = new User();
        user.setId(1L);
        user.setIsKt(false);

        RequestContentAccessCommand cmd = new RequestContentAccessCommand();
        cmd.setUserId(1L);
        cmd.setBookId(2L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.requestContentAccess(cmd);

        verify(outboxMessageRepository).save(argThat(msg -> msg.getEventType().equals("AccessRequestedWithPoints")));
    }

    @Test
    @DisplayName("독서 기록 추가")
    void updateBookRead_shouldAddBookToHistory() {
        User user = new User();
        user.setId(1L);
        user.setMyBookHistory(new ArrayList<>());

        BookViewed event = new BookViewed();
        event.setUserId(1L);
        event.setBookId(42L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.updateBookRead(event);

        Assertions.assertTrue(user.getMyBookHistory().contains(42L));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("구독 상태 조회")
    void getSubscriptionStatus_shouldReturnCorrectValue() {
        User user = new User();
        user.setHasActiveSubscription(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        boolean result = userService.getSubscriptionStatus(1L);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("열람 내역 조회")
    void getContentHistory_shouldReturnHistory() {
        User user = new User();
        user.setMyBookHistory(List.of(1L, 2L, 3L));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        List<Long> result = userService.getContentHistory(1L);

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(2L));
    }

    @Test
    @DisplayName("작가 승인 이벤트 처리 시 isAuthor=true로 변경")
    void authorApproved_should_setIsAuthorTrue() {
        User user = new User();
        user.setId(1L);
        user.setIsAuthor(false);

        AuthorAccepted event = new AuthorAccepted(2L, 1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.authorApproved(event);

        Assertions.assertTrue(user.getIsAuthor());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserInfo()는 UserInfoResponse를 반환한다")
    void getUserInfo_should_return_dto() {
        User user = new User();
        user.setId(1L);
        user.setEmail("dto@example.com");
        user.setIsKt(true);
        user.setIsAuthor(false);
        user.setHasActiveSubscription(false);
        user.setMyBookHistory(List.of(10L, 11L));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserInfoResponse dto = userService.getUserInfo(1L);

        Assertions.assertEquals("dto@example.com", dto.email());
        Assertions.assertTrue(dto.isKT());
        Assertions.assertEquals(List.of(10L, 11L), dto.contentHistory());
    }
}
