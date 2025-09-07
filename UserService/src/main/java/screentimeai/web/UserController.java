////package screentimeai.web;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import screentimeai.application.UserService;
//import screentimeai.domain.User;
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//@RequiredArgsConstructor
//public class UserController {
//    private final UserService userService;
//    @PostMapping("/signup")
//    public ResponseEntity<String> signup(@RequestParam String nickname, @RequestParam String rawPassword) {
//        userService.signup(nickname,rawPassword);
//        return ResponseEntity.ok("✅ Signup success: " + nickname);
//    }
//
//    @GetMapping("/all")
//    public List<User> findAll() {
//        return userService.findAll();  // UserService에서 userRepository.findAll() 리턴하는 메서드 추가
//    }
//}

