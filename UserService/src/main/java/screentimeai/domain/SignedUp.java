package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class SignedUp extends AbstractEvent {

    private Long id;
    private String nickname;

    public SignedUp(User aggregate) {
        super(aggregate);
    }

    public SignedUp() {
        super();
    }
}
//>>> DDD / Domain Event
