package si.um.feri.navigator.utils;

public class Geolocation {
    public double lat;
    public double lng;

    public Geolocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Geolocation{lat=" + lat + ", lng=" + lng + "}";
    }
}
