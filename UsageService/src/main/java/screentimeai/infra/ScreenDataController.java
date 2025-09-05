package screentimeai.infra;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import screentimeai.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/screenData")
@Transactional
public class ScreenDataController {

    @Autowired
    ScreenDataRepository screenDataRepository;
}
//>>> Clean Arch / Inbound Adaptor
