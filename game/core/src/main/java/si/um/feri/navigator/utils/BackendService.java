package si.um.feri.navigator.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.MarkerType;
import si.um.feri.navigator.OOP.Path;
import si.um.feri.navigator.OOP.Station;
import si.um.feri.navigator.OOP.StationType;

public class BackendService {

    public interface MarkerCallback {
        void onSuccess(ArrayList<Marker> markers);
        void onError(Throwable t);
    }

    public interface PathCallback {
        void onSuccess(ArrayList<Path> paths);
        void onError(Throwable t);
    }


    private final Texture hospitalIcon;
    private final Texture fireIcon;
    private final Texture policeIcon;
    private final Texture accidentIcon;

    public BackendService() {
        hospitalIcon = new Texture(Gdx.files.internal("icons/hospital.png"));
        fireIcon = new Texture(Gdx.files.internal("icons/firestation.png"));
        policeIcon = new Texture(Gdx.files.internal("icons/policestation.png"));
        accidentIcon = new Texture(Gdx.files.internal("icons/accidentPlaceHolder.png"));

    }

    public void fetchMarkers(MarkerCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/station/all";
                HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                String jsonString = res.body();
                JsonArray arr = JsonParser.parseString(jsonString).getAsJsonArray();

                ArrayList<Station> stations = new ArrayList<>();
                ArrayList<Marker> markers = new ArrayList<>();

                for (int i = 0; i < arr.size(); i++) {
                    JsonObject obj = arr.get(i).getAsJsonObject();

                    String id = obj.get("_id").getAsString();
                    boolean isPermanent = obj.get("isPermanent").getAsBoolean();

                    JsonArray coords = obj.getAsJsonObject("locationId")
                        .getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates");

                    double lng = coords.get(1).getAsDouble();
                    double lat = coords.get(0).getAsDouble();

                    String typeStr = obj.get("typeOfStation").getAsString();
                    StationType type;
                    Texture icon;
                    switch(typeStr.toLowerCase()) {
                        case "gasilci":
                            icon = fireIcon;
                            type = StationType.GASILSKA;
                            break;
                        case "policijska":
                            icon = policeIcon;
                            type = StationType.POLICIJSKA;
                            break;
                        default:
                            icon = hospitalIcon;
                            type = StationType.BOLNICA;
                            break;
                    }
                    Station station = new Station(type,lat,lng,id, isPermanent);
                    stations.add(station);

                    Marker marker = new Marker(MarkerType.POSTAJA, lat, lng, icon);
                    marker.station = station;
                    markers.add(marker);
                }

                // toti NESRECA je samo da deluje martionv del za izris poti
                markers.add(new Marker(MarkerType.NESRECA, 46.5000, 14.9500, accidentIcon));

                Gdx.app.postRunnable(() -> callback.onSuccess(markers));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }

    public void fetchPaths(PathCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/path/all";
                HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();

                HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
                JsonArray pathsJson = root.getAsJsonArray("paths");

                ArrayList<Path> paths = new ArrayList<>();

                for (int i = 0; i < pathsJson.size(); i++) {
                    JsonObject obj = pathsJson.get(i).getAsJsonObject();

                    Path path = new Path();
                    path.id = obj.get("_id").getAsString();

                    JsonArray pts = obj.getAsJsonArray("locationPoints");
                    for (int j = 0; j < pts.size(); j++) {
                        JsonObject p = pts.get(j).getAsJsonObject();
                        path.points.add(new Geolocation(
                            p.get("lat").getAsDouble(),
                            p.get("lng").getAsDouble()
                        ));
                    }

                    paths.add(path);
                }

                Gdx.app.postRunnable(() -> callback.onSuccess(paths));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }

}
