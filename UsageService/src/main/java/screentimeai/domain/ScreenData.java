package screentimeai.domain;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import screentimeai.UsageserviceApplication;

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

    private Date time; //TODO: time DATE 타입 맞는 지 확인하기

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
