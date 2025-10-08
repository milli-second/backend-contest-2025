package screentimeai.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SignupRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9]{5,20}$",
            message = "닉네임은 영문과 숫자 조합으로 5~20자 이내여야 합니다."
    )
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 8~20자이며, 영문, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }
}
