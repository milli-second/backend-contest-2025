package screentimeai.api;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import screentimeai.application.ScreenDataService;
import screentimeai.dto.ScreenDataRequest;

import java.util.List;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/screenData")
@RequiredArgsConstructor
@Transactional
public class ScreenDataController {

    private final ScreenDataService service;

    @PostMapping
    public ResponseEntity<String> saveScreenData(@RequestBody List<ScreenDataRequest> body) {
        int savedCount = service.saveAll(body);
        return ResponseEntity.ok("✅ Saved " + savedCount + " new records");
    }
}
//>>> Clean Arch / Inbound Adaptor
