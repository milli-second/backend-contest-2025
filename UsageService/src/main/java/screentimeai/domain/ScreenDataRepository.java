package screentimeai.domain;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import screentimeai.domain.*;

import java.util.List;
import java.util.Map;

//<<< PoEAA / Repository
//@RepositoryRestResource(
//    collectionResourceRel = "screenData",
//    path = "screenData"
//)
@Repository
public interface ScreenDataRepository
    extends JpaRepository<ScreenData, Long> {
    // 중복 체크: subject_id + timestamp
    boolean existsBySubjectIdAndTimestampEpochMs(String subjectId, Long timestampEpochMs);

    List<ScreenData> findBySubjectIdAndTypeInAndTimestampEpochMsBetweenOrderByTimestampEpochMs(
            String subjectId,
            List<String> types,
            Long start,
            Long end
    );

    @Query(value = """

            WITH sessions AS (
            SELECT
                s.package_name,
                TO_TIMESTAMP(s.timestamp / 1000) AS start_time,
                LEAD(TO_TIMESTAMP(s.timestamp / 1000)) OVER (
                    PARTITION BY s.package_name
                    ORDER BY TO_TIMESTAMP(s.timestamp / 1000)
                ) AS end_time
            FROM screen_data_table s
            WHERE s.subject_id = :subjectId
              AND s.type = 'MOVE_TO_FOREGROUND'
              AND TO_TIMESTAMP(s.timestamp / 1000) BETWEEN NOW() - INTERVAL '24 HOURS' AND NOW()
        ),
        durations AS (
            SELECT
                COALESCE(m.playstore_category, '기타') AS category,
                EXTRACT(EPOCH FROM (s.end_time - s.start_time)) AS duration_seconds
            FROM sessions s
            LEFT JOIN matched_app_category m
                ON s.package_name = m.package_name
            WHERE s.end_time IS NOT NULL
        )
        SELECT
            category,
            ROUND(SUM(duration_seconds) / 60) AS usage_minutes,
            ROUND(SUM(duration_seconds) * 100.0 / SUM(SUM(duration_seconds)) OVER (), 1) AS usage_percentage
        FROM durations
        GROUP BY category
        ORDER BY usage_minutes DESC
        
        """, nativeQuery = true)
    List<Map<String, Object>> getCategoryStatsLast24Hours(@Param("subjectId") String subjectId);
}
