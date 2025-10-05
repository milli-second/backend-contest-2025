package screentimeai.api;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import screentimeai.domain.ScreenData;
import screentimeai.domain.ScreenDataRepository;
import screentimeai.dto.ScreenDataRequest;

import java.util.ArrayList;
import java.util.List;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/screenData")
@RequiredArgsConstructor
@Transactional
public class ScreenDataController {

    private final ScreenDataRepository screenDataRepository;

    @PostMapping
    public ResponseEntity<String> saveScreenData(@RequestBody List<ScreenDataRequest> body) {

        List<ScreenData> toSave = new ArrayList<>();

        for (ScreenDataRequest req : body) {

            // 중복 확인: subject_id + timestamp
            if (screenDataRepository.existsBySubjectIdAndTimestampEpochMs(
                    req.getSubjectId(),
                    req.getTimestamp()
            )) continue;

            ScreenData entity = new ScreenData();
            entity.setSubjectId(req.getSubjectId());
            entity.setTimestampEpochMs(req.getTimestamp());
            entity.setSourceType(req.getSourceType());
            entity.setSourceInfo(req.getSourceInfo());
            entity.setPackageName(req.getPackageName());
            entity.setName(req.getName());
            entity.setSystemApp(req.getIsSystemApp() != null ? req.getIsSystemApp() : Boolean.FALSE);
            entity.setUpdatedSystemApp(req.getIsUpdatedSystemApp());
            entity.setType(req.getType());

            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            screenDataRepository.saveAll(toSave);
        }

        return ResponseEntity.ok("✅ Saved " + toSave.size() + " new records");
    }
}
//>>> Clean Arch / Inbound Adaptor
