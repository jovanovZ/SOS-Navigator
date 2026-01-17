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
import si.um.feri.navigator.OOP.TrafficPoint;

public class BackendService {

    public interface MarkerCallback {
        void onSuccess(ArrayList<Marker> markers);
        void onError(Throwable t);
    }

    public interface PathCallback {
        void onSuccess(ArrayList<Path> paths);
        void onError(Throwable t);
    }

    public interface TrafficCallback {
        void onSuccess(ArrayList<TrafficPoint> trafficPoints);
        void onError(Throwable t);
    }


    public interface SingleTrafficCallback {
        void onSuccess(TrafficPoint trafficPoint);
        void onError(Throwable t);
    }




    private final Texture hospitalIcon;
    private final Texture fireIcon;
    private final Texture policeIcon;
    private final Texture accidentIcon;
    private final Texture trafficIcon;

    public BackendService() {
        hospitalIcon = new Texture(Gdx.files.internal("icons/hospital.png"));
        fireIcon = new Texture(Gdx.files.internal("icons/firestation.png"));
        policeIcon = new Texture(Gdx.files.internal("icons/policestation.png"));
        accidentIcon = new Texture(Gdx.files.internal("icons/accidentPlaceHolder.png"));
        trafficIcon = new Texture(Gdx.files.internal("icons/traffic.png"));

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

    public void fetchTrafficPoints(TrafficCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/traffic/all";
                HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();

                HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
                JsonArray trafficJson = root.getAsJsonArray("traffic");

                ArrayList<TrafficPoint> trafficPoints = new ArrayList<>();

                for (int i = 0; i < trafficJson.size(); i++) {
                    JsonObject obj = trafficJson.get(i).getAsJsonObject();

                    String id = obj.get("_id").getAsString();
                    String status = obj.get("status").getAsString();
                    int vehicleCount = obj.get("vehicle_count").getAsInt();

                    JsonArray coords = obj.getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates");

                    double lat = coords.get(0).getAsDouble();
                    double lng = coords.get(1).getAsDouble();

                    TrafficPoint trafficPoint = new TrafficPoint(
                        status, lat, lng, id, vehicleCount, null
                    );

                    trafficPoint.icon = trafficIcon;

                    trafficPoints.add(trafficPoint);
                }

                Gdx.app.postRunnable(() -> callback.onSuccess(trafficPoints));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }


    public void fetchTrafficPointById(String id, SingleTrafficCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/traffic/" + id;
                HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();

                HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
                JsonObject trafficObj = root.getAsJsonObject("traffic");

                String trafficId = trafficObj.get("_id").getAsString();
                String status = trafficObj.get("status").getAsString();
                int vehicleCount = trafficObj.get("vehicle_count").getAsInt();
                String imageBase64 = trafficObj.has("image_base64") && !trafficObj.get("image_base64").isJsonNull()
                    ? trafficObj.get("image_base64").getAsString()
                    : null;

                JsonArray coords = trafficObj.getAsJsonObject("geometry")
                    .getAsJsonArray("coordinates");

                double lat = coords.get(0).getAsDouble();
                double lng = coords.get(1).getAsDouble();

                TrafficPoint trafficPoint = new TrafficPoint(
                    status, lat, lng, trafficId, vehicleCount, imageBase64
                );

                trafficPoint.icon = trafficIcon;

                Gdx.app.postRunnable(() -> {
                    trafficPoint.loadImageFromBase64();
                    callback.onSuccess(trafficPoint);
                });

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }



}
