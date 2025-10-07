//db저장 + 이벤트 발행 트리거
package screentimeai.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.ScreenData;
import screentimeai.domain.ScreenDataRepository;
import screentimeai.domain.events.ScreenDataSavedEvent;
import screentimeai.dto.ScreenDataRequest;
import java.util.stream.Collectors;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScreenDataService {

    private final ScreenDataRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public int saveAll(List<ScreenDataRequest> body) {
        List<ScreenData> toSave = new ArrayList<>();

        for (ScreenDataRequest req : body) {
            if (repository.existsBySubjectIdAndTimestampEpochMs(req.getSubjectId(), req.getTimestamp())) {
                continue;
            }
            ScreenData e = new ScreenData();
            e.setSubjectId(req.getSubjectId());
            e.setTimestampEpochMs(req.getTimestamp());
            e.setSourceType(req.getSourceType());
            e.setSourceInfo(req.getSourceInfo());
            e.setPackageName(req.getPackageName());
            e.setName(req.getName());
            e.setSystemApp(Boolean.TRUE.equals(req.getIsSystemApp()));
            e.setUpdatedSystemApp(Boolean.TRUE.equals(req.getIsUpdatedSystemApp()));
            e.setType(req.getType());
            toSave.add(e);
        }

        if (toSave.isEmpty()) return 0;

        List<ScreenData> saved = repository.saveAll(toSave);

        // 저장사실을 이벤트로 알림 (아직 커밋 전이지만, 리스너에서 AFTER_COMMIT로 받음)
        eventPublisher.publishEvent(new ScreenDataSavedEvent(saved));

        return saved.size();
    }

    //1. 주마다 사용패턴
    public List<Map<String, Object>> getWeeklyUsageTrends(String subjectId) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 오늘 날짜 기준
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        // 이번주 월요일
        LocalDate thisWeekMonday = today.minusDays(todayDayOfWeek.getValue() - DayOfWeek.MONDAY.getValue());

        // 지난주 월요일
        LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);

        // 시작 ~ 끝 날짜
        LocalDate startDate = lastWeekMonday;
        LocalDate endDate = today.minusDays(1);;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long from = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long to = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;

            // 하루치 Foreground, Background 이벤트 조회
            List<ScreenData> events = repository.findBySubjectIdAndTypeInAndTimestampEpochMsBetweenOrderByTimestampEpochMs(
                    subjectId,
                    List.of("MOVE_TO_FOREGROUND", "MOVE_TO_BACKGROUND"),
                    from,
                    to
            );

            // 세션 파싱
            List<Pair<ScreenData, ScreenData>> sessionPairs = new ArrayList<>();
            Deque<ScreenData> fgStack = new ArrayDeque<>();
            for (ScreenData event : events) {
                if ("MOVE_TO_FOREGROUND".equals(event.getType())) {
                    fgStack.push(event);
                } else if ("MOVE_TO_BACKGROUND".equals(event.getType()) && !fgStack.isEmpty()) {
                    sessionPairs.add(Pair.of(fgStack.pop(), event));
                }
            }

            while (!fgStack.isEmpty()) {
                ScreenData fg = fgStack.pop();
                sessionPairs.add(Pair.of(fg, ScreenData.withTimestampEpoch(to)));
            }

            // 하루 총 사용 시간 계산 (초 → 분 변환)
            long totalSec = 0;
            for (Pair<ScreenData, ScreenData> session : sessionPairs) {
                long duration = (session.getSecond().getTimestampEpochMs() - session.getFirst().getTimestampEpochMs()) / 1000;
                if (duration > 0) totalSec += duration;
            }

            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("date", date.toString());
            resultMap.put("minutes", totalSec / 60);
            resultList.add(resultMap);
        }

        return resultList;
    }


    //2. 시간대별 사용량
    public List<Map<String, Object>> getTimeSlotUsageForTodayAndYesterday(String subjectId) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        resultList.add(getTimeSlotUsage(subjectId, today));
        resultList.add(getTimeSlotUsage(subjectId, yesterday));

        return resultList;
    }

    public Map<String, Object> getTimeSlotUsage(String subjectId, LocalDate date) {
        ZonedDateTime startOfDay = date.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        long from = startOfDay.toInstant().toEpochMilli();
        long to = endOfDay.toInstant().toEpochMilli();

        //대상 이벤트 조회 (Foreground, Background)
        List<ScreenData> events = repository.findBySubjectIdAndTypeInAndTimestampEpochMsBetweenOrderByTimestampEpochMs(
                subjectId,
                List.of("MOVE_TO_FOREGROUND", "MOVE_TO_BACKGROUND"),
                from,
                to
        );

//        System.out.println("조회된 이벤트 개수: " + events.size());
//        for (ScreenData e : events) {
//            System.out.println(e.getType() + " | " + e.getPackageName() + " | " + e.getTimestampEpochMs());
//        }

        List<Pair<ScreenData, ScreenData>> sessionPairs = new ArrayList<>();
        Deque<ScreenData> foregroundStack = new ArrayDeque<>();


        for (ScreenData event : events) {
            if (event.getType().equals("MOVE_TO_FOREGROUND")) {
                foregroundStack.push(event);  // 시작 저장
            } else if (event.getType().equals("MOVE_TO_BACKGROUND") && !foregroundStack.isEmpty()) {
                ScreenData foreground = foregroundStack.pop();
                sessionPairs.add(Pair.of(foreground, event));
            }
        }

        // Foreground만 남아있다면 현재 시간까지 세션으로 간주
        while (!foregroundStack.isEmpty()) {
            ScreenData fg = foregroundStack.pop();
            sessionPairs.add(Pair.of(fg, ScreenData.withTimestampEpoch(to)));
        }

        //시간대별 duration(초)을 분 단위로 누적
        Map<String, Long> timeSlotMap = new LinkedHashMap<>();
        timeSlotMap.put("야간(00-06시)", 0L);
        timeSlotMap.put("오전(06-12시)", 0L);
        timeSlotMap.put("오후(12-18시)", 0L);
        timeSlotMap.put("저녁(18-24시)", 0L);

//        System.out.println("=== 세션 쌍 목록 ===");
//        for (Pair<ScreenData, ScreenData> session : sessionPairs) {
//            System.out.println("▶ " + session.getFirst().getPackageName()
//                    + ": " + session.getFirst().getTimestampEpochMs()
//                    + " ~ " + session.getSecond().getTimestampEpochMs());
//        }

        for (Pair<ScreenData, ScreenData> session : sessionPairs) {
            LocalDateTime start = Instant.ofEpochMilli(session.getFirst().getTimestampEpochMs())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = Instant.ofEpochMilli(session.getSecond().getTimestampEpochMs())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            long durationSec = Duration.between(start, end).getSeconds();
            if (durationSec <= 0) continue;

            String slot = getTimeSlot(start);
//            System.out.println("▶ [" + slot + "] " + durationSec + "초 (" + durationSec / 60 + "분)");
            timeSlotMap.put(slot, timeSlotMap.get(slot) + durationSec / 60);
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("date", date.toString());
        resultMap.putAll(timeSlotMap);

        return resultMap;
    }

    private String getTimeSlot(LocalDateTime time) {
        int hour = time.getHour();
        if (hour < 6) return "야간(00-06시)";
        if (hour < 12) return "오전(06-12시)";
        if (hour < 18) return "오후(12-18시)";
        return "저녁(18-24시)";
    }

    //3. top3 사용앱
    public List<Map<String, Object>> getTop3UsedApps(String subjectId) {
        long now = System.currentTimeMillis();
        long from = now - 24 * 60 * 60 * 1000L;

        List<ScreenData> events = repository.findBySubjectIdAndTypeInAndTimestampEpochMsBetweenOrderByTimestampEpochMs(
                subjectId,
                List.of("MOVE_TO_FOREGROUND", "MOVE_TO_BACKGROUND"),
                from,
                now
        );

        // 앱별 세션 저장
        Map<String, List<Pair<Long, Long>>> appSessions = new HashMap<>();
        Map<String, Deque<ScreenData>> foregroundStacks = new HashMap<>();

        for (ScreenData event : events) {
            String app = event.getName();
            foregroundStacks.putIfAbsent(app, new ArrayDeque<>());

            if (event.getType().equals("MOVE_TO_FOREGROUND")) {
                foregroundStacks.get(app).push(event);
            } else if (event.getType().equals("MOVE_TO_BACKGROUND") && !foregroundStacks.get(app).isEmpty()) {
                ScreenData fg = foregroundStacks.get(app).pop();
                appSessions.computeIfAbsent(app, k -> new ArrayList<>())
                        .add(Pair.of(fg.getTimestampEpochMs(), event.getTimestampEpochMs()));
            }
        }

        // 아직 종료되지 않은 세션 처리 (현재 시간까지)
        for (Map.Entry<String, Deque<ScreenData>> entry : foregroundStacks.entrySet()) {
            String app = entry.getKey();
            Deque<ScreenData> stack = entry.getValue();
            while (!stack.isEmpty()) {
                ScreenData fg = stack.pop();
                appSessions.computeIfAbsent(app, k -> new ArrayList<>())
                        .add(Pair.of(fg.getTimestampEpochMs(), now));
            }
        }

        // 앱별 총 사용 시간 계산
        Map<String, Long> appUsageSeconds = new HashMap<>();
        for (Map.Entry<String, List<Pair<Long, Long>>> entry : appSessions.entrySet()) {
            long totalSec = 0;
            for (Pair<Long, Long> session : entry.getValue()) {
                totalSec += (session.getSecond() - session.getFirst()) / 1000/ 60;
            }
            appUsageSeconds.put(entry.getKey(), totalSec);
        }

        //top3 정렬
        return appUsageSeconds.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getKey());
                    map.put("usage_minutes", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    //4. 사용 카테고리 분석
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAppCategoryStatsForUser(String subjectId) {
        long toMs = System.currentTimeMillis();
        long fromMs = toMs - 24L * 60 * 60 * 1000;

        List<Map<String, Object>> rows =
                repository.getCategoryStatsLast24Hours(subjectId, fromMs, toMs);
        if (rows == null || rows.isEmpty()) return List.of();

        long totalMinutes = rows.stream()
                .mapToLong(r -> ((Number) r.get("usage_minutes")).longValue())
                .sum();
        if (totalMinutes <= 0) return List.of();

        // minutes DESC
        rows.sort((a, b) -> Long.compare(
                ((Number) b.get("usage_minutes")).longValue(),
                ((Number) a.get("usage_minutes")).longValue()
        ));

        if (rows.size() <= 4) {
            // 카테고리 ≤ 4면 그대로 반환(기타가 포함될 수 있음)
            return rows.stream()
                    .map(r -> toRow(
                            String.valueOf(r.get("category")),
                            ((Number) r.get("usage_minutes")).longValue(),
                            totalMinutes))
                    .toList();
        }

        // 카테고리 > 4면 Top4 + 나머지 합산 → 기타
        List<Map<String, Object>> top4 = new ArrayList<>();
        long othersMinutes = 0;

        for (int i = 0; i < rows.size(); i++) {
            String cat = String.valueOf(rows.get(i).get("category"));
            long minutes = ((Number) rows.get(i).get("usage_minutes")).longValue();

            if (i < 4) {
                top4.add(toRow(cat, minutes, totalMinutes));
            } else {
                othersMinutes += minutes;
            }
        }

        if (othersMinutes > 0) {
            // 만약 Top4 안에 이미 '기타'가 있으면 거기에 합산
            Optional<Map<String, Object>> topEtc = top4.stream()
                    .filter(m -> "기타".equals(m.get("category")))
                    .findFirst();

            if (topEtc.isPresent()) {
                Map<String, Object> etc = topEtc.get();
                long curMin = ((Number) etc.get("usage_minutes")).longValue();
                long newMin = curMin + othersMinutes;
                etc.put("usage_minutes", newMin);
                etc.put("usage_percentage", round1(newMin * 100.0 / totalMinutes));
            } else {
                // Top4에 '기타'가 없으면 새 '기타' 추가 (총 5개가 될 수 있음)
                top4.add(toRow("기타", othersMinutes, totalMinutes));
            }
        }

        // 퍼센트 기준으로 정렬
        top4.sort((a, b) -> Double.compare(
                ((Number) b.get("usage_percentage")).doubleValue(),
                ((Number) a.get("usage_percentage")).doubleValue()
        ));
        return top4;
    }

    private Map<String, Object> toRow(String category, long minutes, long totalMinutes) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("category", category);
        map.put("usage_minutes", minutes);
        map.put("usage_percentage", round1(minutes * 100.0 / totalMinutes));
        return map;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }


}
