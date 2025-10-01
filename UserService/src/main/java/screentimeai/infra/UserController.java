package screentimeai.infra;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import screentimeai.application.UserService;
import screentimeai.domain.User;
import screentimeai.config.security.JwtUtil;
import screentimeai.infra.dto.LoginRequest;
import screentimeai.infra.dto.SignupRequest;

import java.util.Map;

@RestController
@RequestMapping(value="/api/v1/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/test")
    public String test() {
        return "UserService is running!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getNickname(), request.getPassword());
        String token = jwtUtil.generateToken(user.getNickname());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "nickname", user.getNickname()
        ));
    }

    @PostMapping("/logout")
    public String logout(@RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        userService.logout(nickname);
        return "User logged out: " + nickname;
    }

    @PostMapping("/signup")
    public String signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request.getNickname(), request.getPassword());
        return "User signed up: " + request.getNickname();
    }
}
