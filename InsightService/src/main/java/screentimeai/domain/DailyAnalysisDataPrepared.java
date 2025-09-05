package screentimeai.domain;

import java.util.*;
import lombok.*;
import screentimeai.domain.*;
import screentimeai.infra.AbstractEvent;

@Data
@ToString
public class DailyAnalysisDataPrepared extends AbstractEvent {

    private Long userId;
    private Object dailySummary;
}
