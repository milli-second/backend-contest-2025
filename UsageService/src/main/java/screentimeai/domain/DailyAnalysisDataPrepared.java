package screentimeai.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class DailyAnalysisDataPrepared extends AbstractEvent {

    private Long userId;
    private List<String> dailySummary;

    public DailyAnalysisDataPrepared(ScreenData aggregate) {
        super(aggregate);
    }

    public DailyAnalysisDataPrepared() {
        super();
    }
}
//>>> DDD / Domain Event
