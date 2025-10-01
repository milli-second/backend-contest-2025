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

public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String password;
    private boolean active = true;  //  탈퇴 여부 표시
    private String category = null;//초기에는 null처리

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

    public void logout() {
        this.domainEvents.add(new LoggedOut(this));
    }

    public void changePassword(String oldPassword, String newPassword, PasswordEncoder encoder) {
        if (!encoder.matches(oldPassword, this.password)) {
            throw new IllegalArgumentException("❌ 기존 비밀번호가 일치하지 않습니다");
        }
        this.password = encoder.encode(newPassword);
        this.domainEvents.add(new PasswordChanged(this));
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("❌ 이미 탈퇴된 계정입니다.");
        }
        this.active = false;
        this.domainEvents.add(new AccountDeactivated(this));
    }


    @DomainEvents
    public Collection<Object> events() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    public void clearEvents() {
        domainEvents.clear();
    }

//    @PostPersist
//    public void onPostPersist() {
//        this.markSignedUp();
//    }

}



