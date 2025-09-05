package screentimeai.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;
import screentimeai.UsageserviceApplication;
import screentimeai.domain.DailyAnalysisDataPrepared;

@Entity
@Table(name = "ScreenData_table")
@Data
//<<< DDD / Aggregate Root
public class ScreenData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long sequenceId;

    private Integer userId;

    private Date timestamp;

    private Date date;

    private time time;

    private String packageName;

    private Integer dayOfWeek;

    private Integer hourOfDay;

    private Long durationMs;

    private Integer durationMinutes;

    @PostPersist
    public void onPostPersist() {
        DailyAnalysisDataPrepared dailyAnalysisDataPrepared = new DailyAnalysisDataPrepared(
            this
        );
        dailyAnalysisDataPrepared.publishAfterCommit();
    }

    public static ScreenDataRepository repository() {
        ScreenDataRepository screenDataRepository = UsageserviceApplication.applicationContext.getBean(
            ScreenDataRepository.class
        );
        return screenDataRepository;
    }
}
//>>> DDD / Aggregate Root
