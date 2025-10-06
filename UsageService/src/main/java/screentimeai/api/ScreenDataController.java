package screentimeai.api;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import screentimeai.application.ScreenDataService;
import screentimeai.dto.ScreenDataRequest;

import java.util.List;
import java.util.Map;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/api/screenData")
@RequiredArgsConstructor
@Transactional
public class ScreenDataController {

    private final ScreenDataService service;

    @PostMapping
    public ResponseEntity<String> saveScreenData(@RequestBody List<ScreenDataRequest> body) {
        int savedCount = service.saveAll(body);
        return ResponseEntity.ok("✅ Saved " + savedCount + " new records");
    }

    @GetMapping("/weeklyTrend")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTrend(@RequestParam String userId) {
        List<Map<String, Object>> result = service.getWeeklyUsageTrends(userId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/timePattern")
    public ResponseEntity<List<Map<String, Object>>> getTimeSlotUsage(@RequestParam String userId) {
        List<Map<String, Object>> result = service.getTimeSlotUsageForTodayAndYesterday(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<Map<String, Object>>> getTop3Apps(@RequestParam String userId) {
        List<Map<String, Object>> result = service.getTop3UsedApps(userId);
        return ResponseEntity.ok(result);
    }
}
//>>> Clean Arch / Inbound Adaptor
