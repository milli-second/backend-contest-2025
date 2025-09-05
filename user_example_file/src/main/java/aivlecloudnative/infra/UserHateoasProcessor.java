package aivlecloudnative.infra;

import aivlecloudnative.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class UserHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<User>> {

    @Override
    public EntityModel<User> process(EntityModel<User> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/signup")
                .withRel("signup")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/request-subscription"
                )
                .withRel("request-subscription")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/request-content-access"
                )
                .withRel("request-content-access")
        );

        return model;
    }
}
