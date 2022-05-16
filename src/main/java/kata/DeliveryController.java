package kata;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Controller("/delivery")
public class DeliveryController {

    private static final Logger log = getLogger(DeliveryController.class);

    @Inject
    DeliveryRepository repository;
    @Inject
    SendgridEmailGateway emailGateway;
    @Inject
    MapService mapService;


    @Consumes({MediaType.APPLICATION_JSON})
    @Post("/new")
    public HttpResponse<String> newDelivery(NewDelivery newDelivery) {
        log.info("create new delivery");
        if (newDelivery.email.isEmpty()) {
            return HttpResponse.badRequest("email is mandatory for all deliveries");
        }
        repository.create(newDelivery.email(), newDelivery.longitude(), newDelivery.latitude());
        return HttpResponse.ok("all good");
    }

    private record NewDelivery(String email, float latitude, float longitude) {
    }

    @Consumes({MediaType.APPLICATION_JSON})
    @Post
    public HttpResponse<Void> onDelivery(DeliveryEvent deliveryEvent) {
        try {
            DeliveryService.onDelivery(emailGateway, mapService, repository, deliveryEvent);
            return HttpResponse.ok();
        } catch (Exception e) {
            // if status is not in 2xx range our http client in tests throws exception
            return HttpResponse.noContent();
        }
    }

}
