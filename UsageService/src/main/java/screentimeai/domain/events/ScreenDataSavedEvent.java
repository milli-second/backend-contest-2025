package screentimeai.domain.events;

import lombok.Getter;
import org.springframework.beans.BeanUtils;
import screentimeai.domain.ScreenData;

import java.util.List;

@Getter
public class ScreenDataSavedEvent {
    private final List<ScreenData> saved;

    public ScreenDataSavedEvent(List<ScreenData> saved){
        this.saved = saved;
    }
}

