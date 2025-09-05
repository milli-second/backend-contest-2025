package screentimeai.infra;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import screentimeai.domain.*;

import java.util.function.Consumer;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    ScreenDataRepository screenDataRepository;

    @Bean
    public Consumer<String> eventConsumer() {
        return eventString -> {
            // Handle incoming events
            System.out.println("Received event: " + eventString);
        };
    }
}
//>>> Clean Arch / Inbound Adaptor
