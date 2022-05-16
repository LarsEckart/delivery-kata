package kata;

import com.spun.util.Tuple;
import org.lambda.actions.Action1;
import org.lambda.functions.Function1;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class DeliveryService {

    private static final Logger log = getLogger(DeliveryService.class);
    public static final Duration ON_TIME_THRESHOLD = Duration.ofMinutes(10);

    public static void onDelivery(EmailGateway emailGateway, MapService mapService, DeliveryRepository repository, DeliveryEvent deliveryEvent) {
        onDelivery(emailGateway, mapService, deliveryEvent, repository::save, repository.findTodaysDeliveries());
    }

    public static void onDelivery(EmailGateway emailGateway, MapService mapService, DeliveryEvent deliveryEvent, Action1<Delivery> saver, List<Delivery> deliverySchedule) {
        log.info("update delivery");
        IndexSection<Delivery> indexSection = IndexSection.getIndexSection(deliverySchedule, d -> d.getId() == deliveryEvent.id());

        Tuple<Delivery, MyEmail> tuple = handleDelivery(mapService, deliveryEvent, indexSection.current(), indexSection.previous());
        saver.call(tuple.getFirst());
        emailGateway.send(tuple.getSecond());

        getNextDeliveryNotification(mapService, indexSection.next(), deliveryEvent.getLocation()).ifPresent(emailGateway::send);
    }

    private static Tuple<Delivery, MyEmail> handleDelivery(MapService mapService, DeliveryEvent deliveryEvent, Delivery delivery, Delivery previous) {
        delivery.setArrived(true);
        Duration d = Duration.between(delivery.getTimeOfDelivery(), deliveryEvent.timeOfDelivery());
        delivery.setOnTime(d.toMinutes() < ON_TIME_THRESHOLD.toMinutes());
        delivery.setTimeOfDelivery(deliveryEvent.timeOfDelivery());

        updateAverageSpeed(mapService, delivery, previous);
        return new Tuple<>(delivery, getDeliveryEmail(delivery));
    }

    private static double updateAverageSpeed(MapService mapService, Delivery delivery, Delivery previous) {
        if (delivery.isOnTime() || previous == null) {
            return mapService.getAverageSpeed();
        }
        return mapService.updateAverageSpeed(previous.getDeliveryTime(), delivery.getDeliveryTime());
    }

    private static MyEmail getDeliveryEmail(Delivery delivery) {
        String message =
                """
                        Regarding your delivery today at %s.
                        How likely would you be to recommend this delivery service to a friend? 
                                                
                        Click <a href='http://example.com/feedback'>here</a>""".formatted(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(delivery.getTimeOfDelivery()));
        return new MyEmail(delivery.getContactEmail(), "Your feedback is important to us", message);
    }

    public static Optional<MyEmail> getNextDeliveryNotification(MapService mapService, Delivery next, Location previousLocation) {
        if (next == null) {
            return Optional.empty();
        }
        var nextEta = mapService.calculateETA(previousLocation, next.getLocation());
        String subject = "Your delivery will arrive soon";
        var message =
                "Your delivery to [%s,%s] is next, estimated time of arrival is in %s minutes. Be ready!"
                        .formatted(
                                next.getLatitude(),
                                next.getLongitude(),
                                nextEta.toMinutes());
        return Optional.of(new MyEmail(next.getContactEmail(), subject, message));
    }

}
