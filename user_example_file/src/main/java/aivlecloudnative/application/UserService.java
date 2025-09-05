package aivlecloudnative.application;

import aivlecloudnative.domain.AccessRequestedAsSubscriber;
import aivlecloudnative.domain.AccessRequestedWithPoints;
import aivlecloudnative.domain.AuthorAccepted;
import aivlecloudnative.domain.BookViewed;
import aivlecloudnative.domain.LoginCommand;
import aivlecloudnative.domain.LoginResponse;
import aivlecloudnative.domain.OutboxMessage;
import aivlecloudnative.domain.OutboxMessageRepository;
import aivlecloudnative.domain.RequestContentAccessCommand;
import aivlecloudnative.domain.RequestSubscriptionCommand;
import aivlecloudnative.domain.SignUpCommand;
import aivlecloudnative.domain.SignUpResponse;
import aivlecloudnative.domain.User;
import aivlecloudnative.domain.UserInfoResponse;
import aivlecloudnative.domain.UserRepository;
import aivlecloudnative.domain.UserSignedUp;
import aivlecloudnative.domain.UserSubscribed;
import aivlecloudnative.infra.AbstractEvent;
import aivlecloudnative.infra.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public SignUpResponse signUp(SignUpCommand cmd) {
        if (userRepository.existsByEmail(cmd.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.signUp(cmd);

        String hashedPassword = passwordEncoder.encode(cmd.getPassword());
        user.setPassword(hashedPassword);

        User savedUser = userRepository.save(user);

        saveOutbox(new UserSignedUp(savedUser), "UserSignedUp");

        return new SignUpResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUserName()
        );
    }

    public LoginResponse login(LoginCommand cmd) {
        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // ✅ JWT 토큰 생성 (권한 정보 포함)
        String token = jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getIsAuthor(),
                user.getIsAdmin()
        );

        // ✅ 토큰 + 사용자 정보 응답
        return new LoginResponse(
                token,
                "Bearer",
                user.getUserName()
        );
    }

    @Transactional
    public void logout(String token) {
        long exp = jwtTokenProvider.getExpiration(token); // 남은 만료 시간(ms)
        tokenBlacklistService.blacklist(token, exp);
    }

    public User requestSubscription(RequestSubscriptionCommand cmd) {
        User user = findUserByIdOrThrow(cmd.getUserId());

        user.setHasActiveSubscription(true);

        saveOutbox(new UserSubscribed(user), "UserSubscribed");

        return userRepository.save(user);
    }

    @Transactional
    public User requestContentAccess(RequestContentAccessCommand command) {
        Long userId = command.getUserId();
        Long bookId = command.getBookId();

        User user = findUserByIdOrThrow(userId);

        boolean subscribed = user.getHasActiveSubscription();

        AbstractEvent domainEvent = subscribed
                ? new AccessRequestedAsSubscriber(user, bookId)
                : new AccessRequestedWithPoints(user, bookId);

        saveOutbox(domainEvent, domainEvent.getClass().getSimpleName());

        return userRepository.save(user);
    }

    @Transactional
    public void updateBookRead(BookViewed event) {
        User user = findUserByIdOrThrow(event.getUserId());

        Long bookId = event.getBookId();
        user.addBookToHistory(bookId);
        userRepository.save(user);
    }

    private void saveOutbox(AbstractEvent event, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage msg = new OutboxMessage(
                    UUID.randomUUID().toString(),
                    eventType,
                    payload
            );
            outboxMessageRepository.save(msg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("이벤트 JSON 직렬화 실패", e);
        }
    }

    public List<Long> getContentHistory(Long id) {
        User user = findUserByIdOrThrow(id);

        return user.getMyBookHistory();
    }

    public User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다: " + userId));
    }

    public boolean getSubscriptionStatus(Long id) {
        User user = findUserByIdOrThrow(id);

        return user.getHasActiveSubscription();
    }

    @Transactional
    public void authorApproved(AuthorAccepted authorAccepted) {
        Long userId = authorAccepted.getUserId();

        User user = findUserByIdOrThrow(userId);
        user.setIsAuthor(true);

        // 변경사항 저장 (JPA 엔티티이므로 트랜잭션 내에서 dirty checking으로 자동 반영됨)
    }

    public UserInfoResponse getUserInfo(Long id) {
        User user = findUserByIdOrThrow(id);
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getIsKt(),
                user.getIsAuthor(),
                user.getHasActiveSubscription(),
                user.getMyBookHistory()
        );
    }
}
