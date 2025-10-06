package screentimeai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class ScreenDataRequest {

    @JsonProperty("subject_id")
    private String subjectId; //1

    private long timestamp; //1769045960495069

    @JsonProperty("source_type")
    private String sourceType; //phone

    @JsonProperty("source_info")
    private String sourceInfo; //samsung-SM-S938N-15

    @JsonProperty("package_name")
    private String packageName; //com.sec.android.app.launcher

    private String name; //"One UI 홈"

    @JsonProperty("is_system_app")
    private Boolean isSystemApp; //true

    @JsonProperty("is_updated_system_app")
    private Boolean isUpdatedSystemApp; //true

    private String type; //"MOVE_TO_FOREGROUJD"

}
