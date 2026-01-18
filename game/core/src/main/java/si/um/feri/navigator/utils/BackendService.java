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

import si.um.feri.navigator.OOP.Accident;
import si.um.feri.navigator.OOP.AccidentType;
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

    public interface OSMReverseCallback {
        void onSuccess(String displayName, String placeId);
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

                double accLat = 46.5000;
                double accLng = 14.9500; //TO JE DUMMY NESRECA
                Accident accident = new Accident("accident_dummy_1", AccidentType.PROMETNA, accLat, accLng);
                Marker accidentMarker = new Marker(MarkerType.NESRECA, accLat, accLng, accidentIcon);

                accidentMarker.accident = accident;

                markers.add(accidentMarker);
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

    public Texture getAccidentIcon() {
        return accidentIcon;
    }

    public void fetchGeoapifyRoute(double startLat, double startLng, double endLat, double endLng, ZoomXY beginTile, VehiclePathCallback callback) {
        new Thread(() -> {
            try {
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

    public void fetchORSRoute(
        double startLat, double startLng,
        double endLat, double endLng,
        ZoomXY beginTile,
        java.util.List<TrafficPoint> trafficPoints,
        VehiclePathCallback callback
    ) {
        new Thread(() -> {
            try {
                ArrayList<Vector2> baseRoute = fetchORSRouteBlocking(
                    startLat, startLng, endLat, endLng,
                    beginTile,
                    null
                );

                double thresholdMeters = 150.0;
                java.util.List<TrafficPoint> near = findTrafficPointsNearRoute(
                    baseRoute, trafficPoints, beginTile, thresholdMeters
                );

                if (near.isEmpty()) {
                    Gdx.app.postRunnable(() -> callback.onSuccess(baseRoute));
                    return;
                }

                double avoidRadiusMeters = 200.0;
                JsonObject avoidMultiPolygon = buildAvoidMultiPolygon(near, avoidRadiusMeters);

                ArrayList<Vector2> reroute = fetchORSRouteBlocking(
                    startLat, startLng, endLat, endLng,
                    beginTile,
                    avoidMultiPolygon
                );

                Gdx.app.postRunnable(() -> callback.onSuccess(reroute));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }


    private ArrayList<Vector2> fetchORSRouteBlocking(
        double startLat, double startLng,
        double endLat, double endLng,
        ZoomXY beginTile,
        JsonObject avoidMultiPolygonOrNull
    ) throws Exception {

        String uri = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

        JsonObject rootBody = new JsonObject();

        JsonArray coords = new JsonArray();

        JsonArray a = new JsonArray();
        a.add(startLng);
        a.add(startLat);

        JsonArray b = new JsonArray();
        b.add(endLng);
        b.add(endLat);

        coords.add(a);
        coords.add(b);

        rootBody.add("coordinates", coords);

        if (avoidMultiPolygonOrNull != null) {
            JsonObject options = new JsonObject();
            options.add("avoid_polygons", avoidMultiPolygonOrNull);
            rootBody.add("options", options);
        }

        String body = rootBody.toString();

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("Authorization", Keys.OPENROUTESERVICE)
            .header("Content-Type", "application/json")
            .header("Accept", "application/geo+json, application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new Exception("ORS HTTP " + res.statusCode() + " body=" + res.body());
        }

        String respBody = res.body() == null ? "" : res.body();
        JsonObject root = JsonParser.parseString(respBody).getAsJsonObject();
        if (root.has("error")) {
            throw new Exception("ORS returned error: " + root.get("error"));
        }

        JsonArray features = root.getAsJsonArray("features");
        if (features == null || features.size() == 0) {
            throw new Exception("ORS: no features in response");
        }

        JsonObject geom = features.get(0).getAsJsonObject().getAsJsonObject("geometry");
        if (geom == null || !geom.has("coordinates")) {
            throw new Exception("ORS: missing geometry/coordinates");
        }

        JsonArray lineCoords = geom.getAsJsonArray("coordinates");

        ArrayList<Vector2> pathPoints = new ArrayList<>();
        for (int i = 0; i < lineCoords.size(); i++) {
            JsonArray c = lineCoords.get(i).getAsJsonArray();
            double lon = c.get(0).getAsDouble();
            double lat = c.get(1).getAsDouble();

            Vector2 pos = MapRasterTiles.getPixelPosition(
                lat, lon,
                MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                beginTile.x, beginTile.y, Constants.MAP_HEIGHT
            );
            pathPoints.add(pos);
        }

        if (pathPoints.isEmpty()) throw new Exception("ORS: empty polyline");

        return pathPoints;
    }


    private JsonObject buildAvoidMultiPolygon(java.util.List<TrafficPoint> points, double radiusMeters) {
        JsonObject mp = new JsonObject();
        mp.addProperty("type", "MultiPolygon");

        JsonArray multiPolyCoords = new JsonArray();

        for (TrafficPoint tp : points) {
            double lat = tp.geolocation.lat;
            double lon = tp.geolocation.lng;

            JsonArray polygon = new JsonArray();

            JsonArray ring = buildCircleRingLonLat(lon, lat, radiusMeters, 16);

            polygon.add(ring);

            JsonArray multiPolyElement = new JsonArray();
            multiPolyElement.add(polygon);

            multiPolyCoords.add(multiPolyElement);
        }

        mp.add("coordinates", multiPolyCoords);
        return mp;
    }

    private JsonArray buildCircleRingLonLat(double centerLon, double centerLat, double radiusMeters, int steps) {
        double latRad = Math.toRadians(centerLat);
        double latDegPerMeter = 1.0 / 111320.0;
        double lonDegPerMeter = 1.0 / (111320.0 * Math.cos(latRad));

        double dLat = radiusMeters * latDegPerMeter;
        double dLon = radiusMeters * lonDegPerMeter;

        JsonArray ring = new JsonArray();

        for (int i = 0; i < steps; i++) {
            double ang = (2.0 * Math.PI * i) / steps;
            double lon = centerLon + (Math.cos(ang) * dLon);
            double lat = centerLat + (Math.sin(ang) * dLat);

            JsonArray p = new JsonArray();
            p.add(lon);
            p.add(lat);
            ring.add(p);
        }

        if (ring.size() > 0) {
            ring.add(ring.get(0).deepCopy());
        }

        return ring;
    }


    private java.util.List<TrafficPoint> findTrafficPointsNearRoute(
        ArrayList<Vector2> routePixels,
        java.util.List<TrafficPoint> trafficPoints,
        ZoomXY beginTile,
        double thresholdMeters
    ) {
        java.util.List<TrafficPoint> near = new java.util.ArrayList<>();
        if (routePixels == null || routePixels.isEmpty() || trafficPoints == null || trafficPoints.isEmpty()) return near;

        Vector2 base = routePixels.get(0);

        TrafficPoint ref = trafficPoints.get(0);
        double refLat = ref.geolocation.lat;
        double refLon = ref.geolocation.lng;

        Vector2 refPixel = MapRasterTiles.getPixelPosition(
            refLat, refLon,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        double latDegPerMeter = 1.0 / 111320.0;
        double movedLat = refLat + thresholdMeters * latDegPerMeter;

        Vector2 movedPixel = MapRasterTiles.getPixelPosition(
            movedLat, refLon,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        float thresholdPixels = movedPixel.dst(refPixel);

        for (TrafficPoint tp : trafficPoints) {
            Vector2 tpPixel = MapRasterTiles.getPixelPosition(
                tp.geolocation.lat, tp.geolocation.lng,
                MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                beginTile.x, beginTile.y, Constants.MAP_HEIGHT
            );

            boolean isNear = false;
            for (int i = 0; i < routePixels.size(); i += 5) {
                if (routePixels.get(i).dst(tpPixel) <= thresholdPixels) {
                    isNear = true;
                    break;
                }
            }
            if (isNear) near.add(tp);
        }

        return near;
    }

}
