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
    public String login(@RequestParam String nickname, @RequestParam String password) {
        userService.login(nickname, password);
        return "User logged in: " + nickname;
    }
    
    @PostMapping("/logout")
    public String logout(@RequestParam String nickname) {
        userService.logout(nickname);
        return "User logged out: " + nickname;
    }
    
    @PostMapping("/signup")
    public String signup(@RequestParam String nickname, @RequestParam String password) {
        userService.signup(nickname,password);
        return "User signed up: " + nickname;
    }
}
//>>> Clean Arch / Inbound Adaptor
