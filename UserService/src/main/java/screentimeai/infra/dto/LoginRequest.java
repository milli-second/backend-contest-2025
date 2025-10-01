package screentimeai.infra.dto;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    public String getNickname() {
        return nickname;
    }
    public String getPassword() {
        return password;
    }
}
