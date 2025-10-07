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
    WITH base AS (
        SELECT
            s.package_name,
            s.type,
            s."timestamp" AS ts_ms
        FROM screen_data_table s
        WHERE s.subject_id = :subjectId
          AND s."timestamp" BETWEEN :fromMs AND :toMs
    ),
    ord AS (
        SELECT
            package_name,
            type,
            ts_ms,
            LEAD(type)  OVER (PARTITION BY package_name ORDER BY ts_ms) AS next_type,
            LEAD(ts_ms) OVER (PARTITION BY package_name ORDER BY ts_ms) AS next_ms
        FROM base
    ),
    sessions AS (
        SELECT
            package_name,
            ts_ms  AS start_ms,
            next_ms AS end_ms
        FROM ord
        WHERE type = 'MOVE_TO_FOREGROUND'
          AND next_type = 'MOVE_TO_BACKGROUND'
          AND next_ms IS NOT NULL
    ),
    durations AS (
        SELECT
            COALESCE(m.playstore_category, '기타') AS category,
            (end_ms - start_ms) / 1000.0 AS duration_seconds
        FROM sessions s
        LEFT JOIN matched_app_category m
          ON s.package_name = m.package_name
    )
    SELECT
        category,
        CAST(ROUND(SUM(duration_seconds) / 60) AS BIGINT) AS usage_minutes
    FROM durations
    GROUP BY category
    ORDER BY usage_minutes DESC
    """, nativeQuery = true)
    List<Map<String, Object>> getCategoryStatsLast24Hours(
            @Param("subjectId") String subjectId,
            @Param("fromMs") long fromMs,
            @Param("toMs") long toMs
    );

}
