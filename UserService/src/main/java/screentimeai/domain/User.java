package screentimeai.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.persistence.*;
import lombok.Data;
import screentimeai.UserserviceApplication;
import screentimeai.domain.AccountDeactivated;
import screentimeai.domain.LoggedIn;
import screentimeai.domain.LoggedOut;
import screentimeai.domain.PasswordResetted;
import screentimeai.domain.SignedUp;

@Entity
@Table(name = "User_table")
@Data
//<<< DDD / Aggregate Root
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nickname;

    private String category;

    private String password;

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
    }
}
//>>> DDD / Aggregate Root
