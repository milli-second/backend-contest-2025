package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class LoggedOut extends AbstractEvent {

    private Long id;
    private String nickname;

    public LoggedOut(User aggregate) {
        super(aggregate);
    }

    public LoggedOut() {
        super();
    }
}
//>>> DDD / Domain Event
