package screentimeai.domain;

import lombok.*;
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
