package si.um.feri.navigator.OOP;

import si.um.feri.navigator.utils.Geolocation;

public class Station {
    public String id;
    public String locationId;
    public String type;
    public StationType stationType;
    public boolean isPermanent;
    public String region;
    public Geolocation geolocation;

    public Station() {}

    public Station(StationType stationType, double lat, double lng, String id, boolean isPermanent) {
        this.id = id;
        this.isPermanent = isPermanent;
        this.geolocation = new Geolocation(lat, lng);
        this.stationType = stationType;

        switch (stationType) {
            case POLICIJSKA:
                this.type = "Policijska";
                break;
            case GASILSKA:
                this.type = "Gasilci";
                break;
            case BOLNICA:
                this.type = "Bolnica";
                break;
        }
    }
}
