package aivlecloudnative.infra;

import aivlecloudnative.application.UserService;
import aivlecloudnative.domain.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//<<< Clean Arch / Inbound Adaptor
@RestController
@RequestMapping("/users")
@Transactional
@RequiredArgsConstructor        // 생성자 주입
public class UserController {

    private final UserService userService;

    /* ---------- 회원가입 ---------- */
    @PostMapping("/signup")
    public SignUpResponse signUp(@RequestBody @Valid SignUpCommand cmd) {
        return userService.signUp(cmd);
    }

    /* ---------- 로그인 ---------- */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginCommand cmd) {
        return userService.login(cmd);
    }

    /* ---------- 로그아웃 ---------- */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);   // "Bearer " 제거
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    /* ---------- 구독 신청 ---------- */
    @PostMapping("/request-subscription")
    public User requestSubscription(@RequestBody @Valid RequestSubscriptionCommand cmd) {
        return userService.requestSubscription(cmd);
    }

    /* ---------- 열람 신청 ---------- */
    @PostMapping("/request-content-access")
    public User requestContentAccess(@RequestBody @Valid RequestContentAccessCommand cmd) {
        return userService.requestContentAccess(cmd);
    }

    /* ---------- 구독 상태 조회 ---------- */
    @GetMapping("/{id}/is-subscribed")
    public boolean getSubscriptionStatus(@PathVariable Long id) {
        return userService.getSubscriptionStatus(id);
    }

    /* ---------- 열람 이력 조회 ---------- */
    @GetMapping("/{id}/content-histories")
    public List<Long> getContentHistories(@PathVariable Long id) {
        return userService.getContentHistory(id);
    }

    /* ---------- 사용자 정보 조회 ---------- */
    @GetMapping("/{id}")
    public UserInfoResponse getUserInfo(@PathVariable Long id) {
        return userService.getUserInfo(id);
    }
}

//>>> Clean Arch / Inbound Adaptor
