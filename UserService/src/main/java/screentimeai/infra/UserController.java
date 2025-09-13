package screentimeai.infra;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import screentimeai.application.UserService;
import screentimeai.domain.User;
import screentimeai.infra.security.JwtUtil;

import java.util.Map;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;

    }
    @GetMapping("/test")
    public String test() {
        return "UserService is running!";
    }
    
    /*@PostMapping("/login")
    public String login(@RequestParam String nickname, @RequestParam String password) {
        userService.login(nickname, password);
        return "User logged in: " + nickname;
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String nickname, @RequestParam String password) {
        User user = userService.login(nickname, password);
        String token = jwtUtil.generateToken(String.valueOf(user));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "nickname", user.getNickname()
        ));
    }
    
    @PostMapping("/logout")
    public String logout(@RequestParam String nickname) {
        System.out.println("✅ User logout start: " + nickname);

        userService.logout(nickname);
        return "User logged out: " + nickname;
    }
    
    @PostMapping("/signup")
    public String signup(@RequestParam String nickname, @RequestParam String password) {
        System.out.println("📥 nickname raw param: [" + nickname + "]");
        userService.signup(nickname,password);
        return "User signed up: " + nickname;
    }
}
//>>> Clean Arch / Inbound Adaptor
