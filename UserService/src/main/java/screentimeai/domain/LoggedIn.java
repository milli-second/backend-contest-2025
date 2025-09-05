package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class LoggedIn extends AbstractEvent {

    private Long id;
    private String nickname;

    public LoggedIn(User aggregate) {
        super(aggregate);
    }

    public LoggedIn() {
        super();
    }
}
//>>> DDD / Domain Event
