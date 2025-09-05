package screentimeai.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import screentimeai.domain.*;

@Component
public class ScreenDataHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<ScreenData>> {

    @Override
    public EntityModel<ScreenData> process(EntityModel<ScreenData> model) {
        return model;
    }
}
