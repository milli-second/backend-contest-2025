package screentimeai.domain.events;

import lombok.Getter;
import org.springframework.beans.BeanUtils;
import screentimeai.domain.ScreenData;

@Getter
public class ScreenDataSaved extends ScreenData {
    private Long sequenceId;
    private String userId;
    private String packageName;

    public ScreenDataSaved(ScreenData aggregate){
        //super(aggregate);
        BeanUtils.copyProperties(aggregate, this);
    }
}

