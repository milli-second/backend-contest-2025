package screentimeai.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import screentimeai.UserserviceApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String category;//초기에는 null처리
    private String password;

    /*
    @PostPersist
    public void onPostPersist() {
        LoggedIn loggedIn = new LoggedIn(this);
        loggedIn.publishAfterCommit();

        LoggedOut loggedOut = new LoggedOut(this);
        loggedOut.publishAfterCommit();

        SignedUp signedUp = new SignedUp(this);
        signedUp.publishAfterCommit();

        AccountDeactivated accountDeactivated = new AccountDeactivated(this);
        accountDeactivated.publishAfterCommit();

        PasswordResetted passwordResetted = new PasswordResetted(this);
        passwordResetted.publishAfterCommit();
    }

    public static UserRepository repository() {
        UserRepository userRepository = UserserviceApplication.applicationContext.getBean(
            UserRepository.class
        );
        return userRepository;
    }*/
    // 👉 JPA가 DB에 저장하지 않도록 @Transient 붙임
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    // ===============================
    // 이벤트 추가 메서드
    // ===============================
    public void markSignedUp() {
        domainEvents.add(new SignedUp(this));
    }

    public void markLoggedIn() {
        domainEvents.add(new LoggedIn(this));
    }

    public void markLoggedOut() {
        domainEvents.add(new LoggedOut(this));
    }

    // ===============================
    // 스프링 데이터가 자동으로 이벤트 발행할 메서드
    // ===============================
    @DomainEvents
    public Collection<Object> events() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    public void clearEvents() {
        domainEvents.clear();
    }

    // ✅ JPA가 INSERT 끝낸 후 호출됨 → 여기서 markSignedUp 실행
    @PostPersist
    public void onPostPersist() {
        this.markSignedUp();
    }

}
//>>> DDD / Aggregate Root
