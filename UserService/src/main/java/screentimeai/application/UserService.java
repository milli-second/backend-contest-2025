package screentimeai.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.LoggedIn;
import screentimeai.domain.LoggedOut;
import screentimeai.domain.SignedUp;

@Service
@Transactional
public class UserService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public UserService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
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
        
        SignedUp signedUpEvent = new SignedUp();
        signedUpEvent.setId(1L);
        signedUpEvent.setNickname(nickname);
        
        System.out.println("📤 Publishing SignedUp domain event from service");
        applicationEventPublisher.publishEvent(signedUpEvent);
        
        System.out.println("✅ User signup completed: " + nickname);
    }
}