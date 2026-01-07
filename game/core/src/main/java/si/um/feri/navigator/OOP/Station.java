package si.um.feri.navigator.OOP;

import si.um.feri.navigator.utils.Geolocation;

public class Station {
    public Geolocation geolocation;
    public StationType type;
    public String id;
    public boolean isPermanent;

    public Station(StationType type, double lat, double lng, String id, boolean isPermanent) {
        this.type = type;
        this.geolocation = new Geolocation(lat, lng);
        this.id = id;
        this.isPermanent = isPermanent;
    }
}
