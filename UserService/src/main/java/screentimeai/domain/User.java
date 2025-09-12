package screentimeai.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
/*public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String category;//초기에는 null처리
    private String password;

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
        this.markLoggedIn();
        this.markLoggedOut();
    }


}
//>>> DDD / Aggregate Root*/

public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String password;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    public void signup(String rawPassword, PasswordEncoder encoder) {
        this.password = encoder.encode(rawPassword);
        this.domainEvents.add(new SignedUp(this));
    }

    public void login(String rawPassword, PasswordEncoder encoder) {
        if (!encoder.matches(rawPassword, this.password)) {
            throw new IllegalArgumentException("❌ 비밀번호 불일치");
        }
        this.domainEvents.add(new LoggedIn(this));
    }
    public void markSignedUp() {
        domainEvents.add(new SignedUp(this));
    }

    public void logout() {
        this.domainEvents.add(new LoggedOut(this));
    }

    @DomainEvents
    public Collection<Object> events() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    public void clearEvents() {
        domainEvents.clear();
    }

    @PostPersist
    public void onPostPersist() {
        this.markSignedUp();
    }

}



