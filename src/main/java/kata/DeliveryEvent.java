package kata;

import java.time.LocalDateTime;

public record DeliveryEvent(long id, LocalDateTime timeOfDelivery, float latitude, float longitude) {
    public Location getLocation() {
        return new Location(latitude(), longitude());
    }
}
