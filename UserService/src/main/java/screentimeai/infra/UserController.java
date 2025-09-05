package screentimeai.infra;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.*;
import screentimeai.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/users")
@Transactional
public class UserController {

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    StreamBridge streamBridge;
    
    @GetMapping("/test")
    public String test() {
        return "UserService is running!";
    }
    
    @PostMapping("/send-message")
    public String sendMessage(@RequestParam String message) {
        String fullMessage = "Test message from UserService: " + message;
        streamBridge.send("userEventOutput", fullMessage);
        System.out.println("📤 메시지 발송: " + fullMessage);
        return "Message sent: " + message;
    }
}
//>>> Clean Arch / Inbound Adaptor
