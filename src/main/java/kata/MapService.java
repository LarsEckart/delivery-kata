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

    public Duration calculateETA(float latitude, float longitude,
                                 float otherLatitude, float otherLongitude) {
        var distance = this.calculateDistance(latitude, longitude,
                otherLatitude, otherLongitude);
        if (averageSpeed == 0) {
            averageSpeed = DEFAULT_AVERAGE_SPEED;
        }
        Double v = distance / this.averageSpeed * MINUTES_PER_HOUR;
        return Duration.ofMinutes(v.longValue());
    }

    public void updateAverageSpeed(Duration elapsedTime,
                                   float latitude, float longitude, float otherLatitude, float otherLongitude) {
        var distance = this.calculateDistance(latitude, longitude, otherLatitude, otherLongitude);
        var updatedSpeed = distance / (elapsedTime.getSeconds() / (double) SECONDS_PER_HOUR);
        this.averageSpeed = updatedSpeed;
    }

    private double calculateDistance(float latitude, float longitude, float otherLatitude, float otherLongitude) {
        var d1 = latitude * (Math.PI / 180.0);
        var num1 = longitude * (Math.PI / 180.0);
        var d2 = otherLatitude * (Math.PI / 180.0);
        var num2 = otherLongitude * (Math.PI / 180.0) - num1;
        var d3 = Math.pow(Math.sin((d2 - d1) / 2.0), 2.0) + Math.cos(d1) * Math.cos(d2) * Math.pow(
                Math.sin(num2 / 2.0), 2.0);

        return R * (2.0 * Math.atan2(Math.sqrt(d3), Math.sqrt(1.0 - d3)));
    }
}
