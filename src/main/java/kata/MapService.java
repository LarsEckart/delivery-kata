package kata;

import jakarta.inject.Singleton;

import java.time.Duration;

/*
Somewhat naive implementation/calculations but good enough for now.
 */
@Singleton
public class MapService {

    private static final double DEFAULT_AVERAGE_SPEED = 50.0;
    // in km/h
    private double averageSpeed = DEFAULT_AVERAGE_SPEED;

    private final int MINUTES_PER_HOUR = 60;
    private final int SECONDS_PER_HOUR = 3600;
    private final double R = 6373.0;

    public Duration calculateETA(Location start, Location end) {
        var distance = this.calculateDistance(start, end);
        if (averageSpeed == 0) {
            averageSpeed = DEFAULT_AVERAGE_SPEED;
        }
        Double v = distance / this.averageSpeed * MINUTES_PER_HOUR;
        return Duration.ofMinutes(v.longValue());
    }

    private double calculateDistance(Location start, Location end) {
        var d1 = start.latitudeInRadians();
        var num1 = start.longitudeInRadians();
        var d2 = end.latitudeInRadians();
        var num2 = end.longitudeInRadians() - num1;
        var d3 = Math.pow(Math.sin((d2 - d1) / 2.0), 2.0) + Math.cos(d1) * Math.cos(d2) * Math.pow(
                Math.sin(num2 / 2.0), 2.0);

        return R * (2.0 * Math.atan2(Math.sqrt(d3), Math.sqrt(1.0 - d3)));
    }

    public double updateAverageSpeed(DeliveryTime time1, DeliveryTime time2) {
        if (time1 == null || time2 == null) {
            return this.averageSpeed;
        }
        Duration elapsedTime = Duration.between(time1.time(), time2.time());
        var distance = this.calculateDistance(time1.location(), time2.location());
        var updatedSpeed = distance / (elapsedTime.getSeconds() / (double) SECONDS_PER_HOUR);
        this.averageSpeed = updatedSpeed;
        return this.averageSpeed;
    }

    public double getAverageSpeed() {
        return this.averageSpeed;
    }
}
