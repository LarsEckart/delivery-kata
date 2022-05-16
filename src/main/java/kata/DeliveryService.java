package kata;

import org.lambda.actions.Action1;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class DeliveryService {

    private static final Logger log = getLogger(DeliveryService.class);

    public static void onDelivery(EmailGateway emailGateway, MapService mapService, DeliveryRepository repository, DeliveryEvent deliveryEvent) {
        Action1<Delivery> saver = repository::save;
        List<Delivery> deliverySchedule = repository.findTodaysDeliveries();

        onDelivery(emailGateway, mapService, deliveryEvent, saver, deliverySchedule);
    }

    public static void onDelivery(EmailGateway emailGateway, MapService mapService, DeliveryEvent deliveryEvent, Action1<Delivery> saver, List<Delivery> deliverySchedule) {
        log.info("update delivery");
        Delivery nextDelivery = null;
        for (int i = 0; i < deliverySchedule.size(); i++) {
            Delivery delivery = deliverySchedule.get(i);
            if (deliveryEvent.id() == delivery.getId()) {
                delivery.setArrived(true);
                Duration d = Duration.between(delivery.getTimeOfDelivery(), deliveryEvent.timeOfDelivery());

                // fast delivery when less than fifteen minutes
                if (d.toMinutes() < 10 == true)
                    delivery.setOnTime(true);

                delivery.setTimeOfDelivery(deliveryEvent.timeOfDelivery());
                String message =
                        """
                                Regarding your delivery today at %s.
                                How likely would you be to recommend this delivery service to a friend? 
                                                        
                                Click <a href='http://example.com/feedback'>here</a>""".formatted(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(delivery.getTimeOfDelivery()));
                emailGateway.send(delivery.getContactEmail(), "Your feedback is important to us", message);
                if (deliverySchedule.size() > i + 1) {
                    nextDelivery = deliverySchedule.get(i + 1);
                }

                if (!delivery.isOnTime() && deliverySchedule.size() > 1 && i > 0) {
                    var previousDelivery = deliverySchedule.get(i - 1);
                    Duration elapsedTime =
                            Duration.between(previousDelivery.getTimeOfDelivery(), delivery.getTimeOfDelivery());
                    mapService.updateAverageSpeed(
                            elapsedTime, previousDelivery.getLatitude(),
                            previousDelivery.getLongitude(), delivery.getLatitude(),
                            delivery.getLongitude());
                }
                saver.call(delivery);
            }
        }

        handleNextDelivery(emailGateway, mapService, deliveryEvent, nextDelivery);
    }

    public static void handleNextDelivery(EmailGateway emailGateway, MapService mapService, DeliveryEvent deliveryEvent, Delivery nextDelivery) {
        if (nextDelivery == null) {
            return;
        }
        var nextEta = mapService.calculateETA(
                deliveryEvent.latitude(), deliveryEvent.longitude(),
                nextDelivery.getLatitude(), nextDelivery.getLongitude());
        String subject = "Your delivery will arrive soon";
        var message =
                "Your delivery to [%s,%s] is next, estimated time of arrival is in %s minutes. Be ready!"
                        .formatted(
                                nextDelivery.getLatitude(),
                                nextDelivery.getLongitude(),
                                nextEta.getSeconds() / 60);
        emailGateway.send(nextDelivery.getContactEmail(), subject, message);
    }
}
