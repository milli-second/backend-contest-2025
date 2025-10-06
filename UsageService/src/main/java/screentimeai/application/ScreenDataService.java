//db저장 + 이벤트 발행 트리거
package screentimeai.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import screentimeai.domain.ScreenData;
import screentimeai.domain.ScreenDataRepository;
import screentimeai.domain.events.ScreenDataSavedEvent;
import screentimeai.dto.ScreenDataRequest;

import java.util.ArrayList;
import java.util.List;

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
}
