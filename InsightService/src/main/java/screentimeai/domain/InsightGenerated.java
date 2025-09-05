package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class InsightGenerated extends AbstractEvent {

    private Long id;
    private List<String> insightReport;

    public InsightGenerated(Insight aggregate) {
        super(aggregate);
    }

    public InsightGenerated() {
        super();
    }
}
//>>> DDD / Domain Event
