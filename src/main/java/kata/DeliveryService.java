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
        Action1<Delivery> saver = repository::save;
        List<Delivery> deliverySchedule = repository.findTodaysDeliveries();

        onDelivery(emailGateway, mapService, deliveryEvent, saver, deliverySchedule);
    }

    public static void onDelivery(EmailGateway emailGateway, MapService mapService, DeliveryEvent deliveryEvent, Action1<Delivery> saver, List<Delivery> deliverySchedule) {
        log.info("update delivery");
        int index = getIndexOfDelivery(deliverySchedule, (Delivery d) -> d.getId() == deliveryEvent.id());

        Delivery delivery = deliverySchedule.get(index);
        Delivery previous = 0 < index ? deliverySchedule.get(index - 1) : null;
        Tuple<Delivery, MyEmail> tuple = handleDelivery(mapService, deliveryEvent, delivery, previous);
        saver.call(tuple.getFirst());
        emailGateway.send(tuple.getSecond());

        Delivery nextDelivery = index < deliverySchedule.size() - 1 ? deliverySchedule.get(index + 1) : null;
        getNextDeliveryNotification(mapService, nextDelivery, deliveryEvent.getLocation()).ifPresent(emailGateway::send);
    }

    private static Tuple<Delivery, MyEmail> handleDelivery(MapService mapService, DeliveryEvent deliveryEvent, Delivery delivery, Delivery previous) {
        delivery.setArrived(true);
        Duration d = Duration.between(delivery.getTimeOfDelivery(), deliveryEvent.timeOfDelivery());
        delivery.setOnTime(d.toMinutes() < ON_TIME_THRESHOLD.toMinutes());
        delivery.setTimeOfDelivery(deliveryEvent.timeOfDelivery());

        updateAverageSpeed(mapService, delivery, previous);
        return new Tuple<>(delivery, getDeliveryEmail(delivery));
    }

    private static void updateAverageSpeed(MapService mapService, Delivery delivery, Delivery previous) {
        if (delivery.isOnTime() || previous == null) {
            return;
        }
        DeliveryTime time1 = new DeliveryTime(previous.getTimeOfDelivery(), previous.getLocation());
        DeliveryTime time2 = new DeliveryTime(delivery.getTimeOfDelivery(), delivery.getLocation());
        mapService.updateAverageSpeed(time1, time2);
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

    private static <T> int getIndexOfDelivery(List<T> deliverySchedule, Function1<T, Boolean> predicate) {
        for (int i = 0; i < deliverySchedule.size(); i++) {
            if (predicate.call(deliverySchedule.get(i))) {
                return i;
            }
        }
        return -1;
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
