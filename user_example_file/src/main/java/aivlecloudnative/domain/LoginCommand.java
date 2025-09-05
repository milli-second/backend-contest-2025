package aivlecloudnative.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginCommand {
    @NotNull(message = "이메일을 입력하세요.")
    private String email;

    @NotNull(message = "비밀번호를 입력하세요.")
    private String password;
}