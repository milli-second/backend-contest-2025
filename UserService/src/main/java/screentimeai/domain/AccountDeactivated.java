package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class AccountDeactivated extends AbstractEvent {

    private Long id;

    public AccountDeactivated(User aggregate) {
        super(aggregate);
    }

    public AccountDeactivated() {
        super();
    }
}
//>>> DDD / Domain Event
