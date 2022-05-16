package kata;

public record Location(float latitude, float longitude) {

    public double latitudeInRadians() {
         return latitude * (Math.PI / 180.0);
    }
    public double longitudeInRadians() {
         return longitude * (Math.PI / 180.0);
    }
}
