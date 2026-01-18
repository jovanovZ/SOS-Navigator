package si.um.feri.navigator.OOP;

import java.util.Date;

import si.um.feri.navigator.utils.Geolocation;

public class Accident {
    public String id;
    public Geolocation geolocation;

    public AccidentType typeOfAccident;
    public int locationFreqMinutes = 1440;
    public Date timeStamp = new Date();

    public String osmDisplayName;
    public String osmPlaceId;

    public Accident(String id, AccidentType type, double lat, double lng) {
        this.id = id;
        this.typeOfAccident = type;
        this.geolocation = new Geolocation(lat, lng);
    }
}
