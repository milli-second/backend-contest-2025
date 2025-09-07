package screentimeai.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.persistence.*;
import lombok.Data;
import screentimeai.InsightserviceApplication;
import screentimeai.domain.InsightGenerated;

@Entity
@Table(name = "Insight_table")
@Data
//<<< DDD / Aggregate Root
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    private List<String> insightReport;

    @PostPersist
    public void onPostPersist() {
        InsightGenerated insightGenerated = new InsightGenerated(this);
        insightGenerated.publishAfterCommit();
    }

    public static InsightRepository repository() {
        InsightRepository insightRepository = InsightserviceApplication.applicationContext.getBean(
            InsightRepository.class
        );
        return insightRepository;
    }
}
//>>> DDD / Aggregate Root
