package si.um.feri.navigator.utils;

import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.MarkerType;
import si.um.feri.navigator.OOP.Path;
import si.um.feri.navigator.OOP.Station;
import si.um.feri.navigator.OOP.StationType;
import si.um.feri.navigator.OOP.TrafficPoint;
import si.um.feri.navigator.OOP.Vehicle;

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

    public interface VehicleCallback {
        void onSuccess(ArrayList<Vehicle> vehicles);
        void onError(Throwable t);
    }

    public interface StationUpdateCallback {
        void onSuccess();
        void onError(Throwable t);
    }

    public Texture getIconForType(String type) {
        switch(type.toLowerCase()) {
            case "gasilci": return fireIcon;
            case "policijska": return policeIcon;
            case "bolnica":
            default: return hospitalIcon;
        }
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

                    String locationId = obj.getAsJsonObject("locationId").get("_id").getAsString();
                    String region = obj.has("region") && !obj.get("region").isJsonNull()
                        ? obj.get("region").getAsString()
                        : "Podravska";

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
                    station.locationId = locationId;
                    station.region = region;
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

    public void fetchVehicles(VehicleCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/vehicle/all";
                HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();

                HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                JsonArray vehiclesJson = JsonParser.parseString(res.body()).getAsJsonArray();

                ArrayList<Vehicle> vehicles = new ArrayList<>();

                for (int i = 0; i < vehiclesJson.size(); i++) {
                    JsonObject obj = vehiclesJson.get(i).getAsJsonObject();

                    String id = obj.get("id").getAsString();
                    String type = obj.get("type").getAsString();
                    float acceleration = obj.get("acceleration").getAsFloat();

                    JsonObject locationStart = obj.getAsJsonObject("locationStart");
                    JsonArray startCoords = locationStart.getAsJsonArray("coordinates");
                    double startLng = startCoords.get(0).getAsDouble();
                    double startLat = startCoords.get(1).getAsDouble();

                    JsonObject locationEnd = obj.getAsJsonObject("locationEnd");
                    JsonArray endCoords = locationEnd.getAsJsonArray("coordinates");
                    double endLng = endCoords.get(0).getAsDouble();
                    double endLat = endCoords.get(1).getAsDouble();

                    Vehicle vehicle = new Vehicle(id, type, acceleration,
                        startLng, startLat, endLng, endLat);

                    vehicle.icon = policeIcon;

                    vehicles.add(vehicle);
                }

                Gdx.app.postRunnable(() -> callback.onSuccess(vehicles));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }
    public interface VehiclePathCallback {
        void onSuccess(ArrayList<Vector2> pathPoints);
        void onError(Throwable t);
    }

    public void fetchGeoapifyRoute(double startLat, double startLng, double endLat, double endLng, ZoomXY beginTile, VehiclePathCallback callback) {
        new Thread(() -> {
            try {
                // Geoapify Routing API endpoint
                String waypoints = startLat + "," + startLng + "|" + endLat + "," + endLng;
                String encodedWaypoints = URLEncoder.encode(waypoints, StandardCharsets.UTF_8);

                String uri = "https://api.geoapify.com/v1/routing" +
                    "?waypoints=" + encodedWaypoints +
                    "&mode=drive" +
                    "&apiKey=" + Keys.GEOAPIFY;

                HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();

                HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();

                if (root.has("error")) {
                    throw new Exception("Geoapify API error: " + root.get("error").getAsString());
                }

                JsonArray features = root.getAsJsonArray("features");
                if (features == null || features.size() == 0) {
                    throw new Exception("No route found in response");
                }

                JsonObject feature = features.get(0).getAsJsonObject();
                JsonObject geometry = feature.getAsJsonObject("geometry");

                String geometryType = geometry.get("type").getAsString();
                JsonArray coordinates = geometry.getAsJsonArray("coordinates");

                ArrayList<Vector2> pathPoints = new ArrayList<>();

                if (geometryType.equals("MultiLineString")) {
                    if (coordinates.size() > 0) {
                        JsonArray lineString = coordinates.get(0).getAsJsonArray();

                        for (int i = 0; i < lineString.size(); i++) {
                            JsonArray coord = lineString.get(i).getAsJsonArray();
                            double lng = coord.get(0).getAsDouble();
                            double lat = coord.get(1).getAsDouble();

                            Vector2 pos = MapRasterTiles.getPixelPosition(
                                lat, lng,
                                MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                                beginTile.x, beginTile.y, Constants.MAP_HEIGHT
                            );
                            pathPoints.add(pos);
                        }
                    }
                } else if (geometryType.equals("LineString")) {
                    for (int i = 0; i < coordinates.size(); i++) {
                        JsonArray coord = coordinates.get(i).getAsJsonArray();
                        double lng = coord.get(0).getAsDouble();
                        double lat = coord.get(1).getAsDouble();

                        Vector2 pos = MapRasterTiles.getPixelPosition(
                            lat, lng,
                            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
                        );
                        pathPoints.add(pos);
                    }
                } else {
                    throw new Exception("Unsupported geometry type: " + geometryType);
                }

                if (pathPoints.isEmpty()) {
                    throw new Exception("No path points generated from route");
                }

                Gdx.app.postRunnable(() -> callback.onSuccess(pathPoints));

            } catch (Exception e) {
                Gdx.app.error("Geoapify", "Failed to fetch route: " + e.getMessage(), e);
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }


    public void updateStation(String stationId, String locationId, String typeOfStation, boolean isPermanent, String region, double newLat, double newLng, StationUpdateCallback callback) {
        new Thread(() -> {
            try {
                String stationUri = Keys.SERVER_URL + "/api/station/update/" + stationId;

                String stationJson = String.format(
                    "{\"locationId\":\"%s\",\"typeOfStation\":\"%s\",\"isPermanent\":%b,\"region\":\"%s\"}",
                    locationId, typeOfStation, isPermanent, region
                );

                HttpRequest stationReq = HttpRequest.newBuilder()
                    .uri(URI.create(stationUri))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(stationJson))
                    .build();

                HttpResponse<String> stationRes = HttpClient.newHttpClient()
                    .send(stationReq, HttpResponse.BodyHandlers.ofString());

                if (stationRes.statusCode() != 200) {
                    throw new Exception("Station update failed: HTTP " + stationRes.statusCode());
                }

                String locationUri = Keys.SERVER_URL + "/api/location/update" + locationId;

                String locationJson = String.format(
                    "{\"geometry\":{\"type\":\"Point\",\"coordinates\":[%f,%f]}}",
                    newLng, newLat
                );

                HttpRequest locationReq = HttpRequest.newBuilder()
                    .uri(URI.create(locationUri))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(locationJson))
                    .build();

                HttpResponse<String> locationRes = HttpClient.newHttpClient()
                    .send(locationReq, HttpResponse.BodyHandlers.ofString());

                if (locationRes.statusCode() != 200) {
                    Gdx.app.log("Backend", "Location update warning: HTTP " + locationRes.statusCode());
                }

                Gdx.app.postRunnable(callback::onSuccess);

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }

    public void deleteStation(String stationId, StationUpdateCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/station/delete/" + stationId;

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .DELETE()
                    .build();

                HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() != 200) {
                    throw new Exception("Delete failed: HTTP " + res.statusCode());
                }

                Gdx.app.postRunnable(callback::onSuccess);

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }
}
