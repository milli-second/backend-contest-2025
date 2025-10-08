//package screentimeai.domain;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import screentimeai.UsageserviceApplication;
//
//import java.util.UUID;
//
//@Entity
//@Table(name = "ScreenData_table")
//@Data
////<<< DDD / Aggregate Root
//public classScreenData {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "subject_id", nullable = false, length = 64)
//    private String subjectId;
//
//    @Column(name = "timestamp", nullable = false)
//    private Long timestampEpochMs;
//
//    @Column(name = "source_type")
//    private String sourceType;
//
//    @Column(name = "source_info", nullable = false)
//    private String sourceInfo;
//
//    @Column(name = "package_name", nullable = false)
//    private String packageName;
//
//    @Column(name = "name", nullable = false)
//    private String name;
//
//    @Column(name = "is_system_app", nullable = false)
//    private Boolean systemApp;
//
//    @Column(name = "is_updated_system_app")
//    private Boolean updatedSystemApp;
//
//    @Column(name = "type", nullable = false)
//    private String type;
//
//    public static ScreenData withTimestampEpoch(long timestampEpochMs) {
//        ScreenData data = new ScreenData();
//        data.setTimestampEpochMs(timestampEpochMs);
//        return data;
//    }
//
//
//
////    @PostPersist
////    public void onPostPersist() {
////        DailyAnalysisDataPrepared dailyAnalysisDataPrepared = new DailyAnalysisDataPrepared(
////            this
////        );
////        dailyAnalysisDataPrepared.publishAfterCommit();
////    }
//
////    public static ScreenDataRepository repository() {
////        ScreenDataRepository screenDataRepository = UsageserviceApplication.applicationContext.getBean(
////            ScreenDataRepository.class
////        );
////        return screenDataRepository;
////    }
//}
////>>> DDD / Aggregate Root
