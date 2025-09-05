package screentimeai.infra;

import org.springframework.web.bind.annotation.*;
import screentimeai.application.UserService;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/users")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/test")
    public String test() {
        return "UserService is running!";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String nickname) {
        userService.login(nickname);
        return "User logged in: " + nickname;
    }
    
    @PostMapping("/logout")
    public String logout(@RequestParam String nickname) {
        userService.logout(nickname);
        return "User logged out: " + nickname;
    }
    
    @PostMapping("/signup")
    public String signup(@RequestParam String nickname) {
        userService.signup(nickname);
        return "User signed up: " + nickname;
    }
}
//>>> Clean Arch / Inbound Adaptor
