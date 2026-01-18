package si.um.feri.navigator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import si.um.feri.navigator.OOP.Accident;
import si.um.feri.navigator.OOP.AccidentType;
import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.MarkerType;
import si.um.feri.navigator.OOP.Path;
import si.um.feri.navigator.OOP.Station;
import si.um.feri.navigator.OOP.TrafficPoint;
import si.um.feri.navigator.OOP.Vehicle;
import si.um.feri.navigator.assets.AssetsDescriptors;
import si.um.feri.navigator.logic.NavigationLogic;
import si.um.feri.navigator.render.MapRenderer;
import si.um.feri.navigator.ui.NavigatorUI;
import si.um.feri.navigator.utils.BackendService;
import si.um.feri.navigator.utils.Constants;
import si.um.feri.navigator.utils.Geolocation;
import si.um.feri.navigator.utils.MapRasterTiles;
import si.um.feri.navigator.utils.ZoomXY;

public class Navigator extends ApplicationAdapter implements GestureDetector.GestureListener {

    private static final float MIN_ZOOM = 0.02f;
    private static final float MAX_ZOOM = 5.0f;
    private static final float WHEEL_ZOOM_FACTOR = 1.15f;
    private static final float CAR_TRAVEL_SECONDS = 10f;
    private static final float CAR_MIN_SPEED = 200f;
    private static final float CAR_MAX_SPEED = 8000f;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;

    private OrthographicCamera camera;
    private Viewport viewport;
    private final Vector3 tmp = new Vector3();

    private ZoomXY beginTile;
    private ZoomXY[] tileZone;

    private final List<Vector2> pathPoints = new ArrayList<>();
    private boolean showPath = false;
    private boolean showPaths = false;

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.12, 14.99);

    private Animation<TextureRegion> carAnim;
    private float carStateTime = 0f;

    private boolean carMoving = false;
    private boolean carVisible = false;

    private float carSpeed = 1400f;
    private int carSegIndex = 0;
    private float carTravelInSeg = 0f;

    private final Vector2 carPos = new Vector2();
    private float carRotationDeg = 0f;

    private float carSpriteW = 1800f;
    private float carSpriteH = 950f;

    private boolean carConstantScreenSize = true;

    private final List<Texture> carFrameTextures = new ArrayList<>();
    private final ArrayList<Marker> MARKERS = new ArrayList<>();

    private final ArrayList<TrafficPoint> TRAFFIC_POINTS = new ArrayList<>();
    private TrafficPoint lastHoveredTrafficPoint = null;
    private final ArrayList<Vehicle> VEHICLES = new ArrayList<>();


    private BackendService backendService;
    private final ArrayList<ArrayList<Vector2>> backendPaths = new ArrayList<>();

    private AssetManager assetManager;

    private final NavigationLogic logic = new NavigationLogic();
    private final NavigatorUI ui = new NavigatorUI();
    private final MapRenderer renderer = new MapRenderer();

    private Marker selectedMarker = null;

    private final ArrayList<Accident> ACCIDENTS = new ArrayList<>();


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, camera);
        viewport.apply();

        assetManager = new AssetManager();
        assetManager.load(AssetsDescriptors.UI_SKIN);
        assetManager.load(AssetsDescriptors.UI_FONT);
        assetManager.finishLoading();

        Skin skin = assetManager.get(AssetsDescriptors.UI_SKIN);
        font = assetManager.get(AssetsDescriptors.UI_FONT);

        camera.zoom = 1.0f;
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.update();

        ACCIDENTS.add(new Accident("acc_001", AccidentType.KRIMINAL, 46.5500, 15.6500));
        ACCIDENTS.add(new Accident("acc_002", AccidentType.PROMETNA, 46.5700, 15.6300));
        ACCIDENTS.add(new Accident("acc_003", AccidentType.ZDRAVSTVENI_PRIMER, 46.5300, 15.6700));
        ACCIDENTS.add(new Accident("acc_004", AccidentType.NARAVNA_NESRECA, 46.5600, 15.6100));
        ACCIDENTS.add(new Accident("acc_005", AccidentType.PROMETNA, 46.5400, 15.6400));

        ui.init(skin, font, this::runSimulation);

        ui.setUpdateListener((marker, newType, newLat, newLng) -> {
            Station s = marker.station;
            if (s == null) return;

            backendService.updateStation(
                s.id,
                s.locationId,
                newType,
                s.isPermanent,
                s.region != null ? s.region : "Podravska",
                newLat,
                newLng,
                new BackendService.StationUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        s.type = newType;
                        s.geolocation.lat = newLat;
                        s.geolocation.lng = newLng;

                        marker.lokacija.lat = newLat;
                        marker.lokacija.lng = newLng;
                        marker.icon = backendService.getIconForType(newType);

                        ui.showStatus("Shranjeno!", Color.GREEN);
                    }

                    @Override
                    public void onError(Throwable t) {
                        ui.showStatus("Napaka!", Color.RED);
                    }
                }
            );
        });

        ui.setDeleteListener(marker -> {
            Station s = marker.station;
            if (s == null) return;
            backendService.deleteStation(s.id, new BackendService.StationUpdateCallback() {
                @Override
                public void onSuccess() {
                    MARKERS.remove(marker);
                    selectedMarker = null;
                    ui.hideInfo();
                }
                @Override
                public void onError(Throwable t) {
                    ui.showStatus("Napaka pri brisanju!", Color.RED);
                }
            });
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(ui.getStage());
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                zoomTowardsMouse(amountY);
                return true;
            }
        });
        multiplexer.addProcessor(new GestureDetector(this));
        Gdx.input.setInputProcessor(multiplexer);

        ZoomXY centerTile = MapRasterTiles.getTileNumber(
            CENTER_GEOLOCATION.lat,
            CENTER_GEOLOCATION.lng,
            Constants.ZOOM
        );

        int offsetX = (Constants.NUM_TILES_X - 1) / 2;
        int offsetY = (Constants.NUM_TILES_Y - 1) / 2;

        beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - offsetX, centerTile.y - offsetY);
        tileZone = MapRasterTiles.getTileZoneCoords(centerTile, Constants.NUM_TILES_X, Constants.NUM_TILES_Y);

        loadCarAnimation();

        backendService = new BackendService();

        backendService.fetchMarkers(new BackendService.MarkerCallback() {
            @Override
            public void onSuccess(ArrayList<Marker> markers) {
                MARKERS.addAll(markers);
                for (Accident acc : ACCIDENTS) {
                    Marker accMarker = new Marker(
                        MarkerType.NESRECA,
                        acc.geolocation.lat,
                        acc.geolocation.lng,
                        backendService.getAccidentIcon()
                    );
                    accMarker.accident = acc;
                    MARKERS.add(accMarker);
                }
            }
            @Override
            public void onError(Throwable t) {
                Gdx.app.error("Backend", "Failed to load markers", t);
            }
        });

        backendService.fetchTrafficPoints(new BackendService.TrafficCallback() {
            @Override
            public void onSuccess(ArrayList<TrafficPoint> trafficPoints) {
                TRAFFIC_POINTS.addAll(trafficPoints);
            }

            @Override
            public void onError(Throwable t) {
                Gdx.app.error("Backend", "Failed to load TRAFFIC", t);
            }
        });

        backendService.fetchVehicles(new BackendService.VehicleCallback() {
            @Override
            public void onSuccess(ArrayList<Vehicle> vehicles) {
                VEHICLES.clear();
                VEHICLES.addAll(vehicles);

                for (Vehicle vehicle : VEHICLES) {
                    backendService.fetchGeoapifyRoute(
                        vehicle.locationStart.lat, vehicle.locationStart.lng,
                        vehicle.locationEnd.lat, vehicle.locationEnd.lng,
                        beginTile,
                        new BackendService.VehiclePathCallback() {
                            @Override
                            public void onSuccess(ArrayList<Vector2> pathPoints) {
                                vehicle.pathPoints = pathPoints;

                                if (!vehicle.pathPoints.isEmpty()) {
                                    vehicle.currentPos.set(vehicle.pathPoints.get(0));
                                    vehicle.segIndex = 0;
                                    vehicle.travelInSeg = 0f;
                                    vehicle.isMoving = true;
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                Gdx.app.error("Vehicle", "Failed to load Geoapify route for " + vehicle.id + ", using fallback", t);

                                generateVehiclePathFallback(vehicle);
                            }
                        }
                    );
                }
            }
            @Override
            public void onError(Throwable t) {
                Gdx.app.error("Backend", "Failed to load vehicles", t);
            }
        });


        backendService.fetchPaths(new BackendService.PathCallback() {
            @Override
            public void onSuccess(ArrayList<Path> paths) {
                backendPaths.clear();

                for (Path p : paths) {
                    ArrayList<Vector2> polyline = new ArrayList<>();
                    for (Geolocation g : p.points) {
                        Vector2 px = MapRasterTiles.getPixelPosition(g.lat, g.lng, MapRasterTiles.TILE_SIZE, Constants.ZOOM, beginTile.x, beginTile.y, Constants.MAP_HEIGHT);
                        polyline.add(px);
                    }
                    backendPaths.add(polyline);
                }

                if (!backendPaths.isEmpty()) {
                    pathPoints.clear();
                    pathPoints.addAll(backendPaths.get(0));
                    showPaths = false;
                }
            }

            @Override public void onError(Throwable t) { Gdx.app.error("Backend", "Failed to fetch paths", t); }
        });
    }

    private void loadCarAnimation() {
        TextureRegion[] frames = new TextureRegion[24];

        for (int i = 0; i < 24; i++) {
            String path = "policecarimages/car_" + (i + 1) + ".png";

            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            carFrameTextures.add(t);
            frames[i] = new TextureRegion(t);
        }

        carAnim = new Animation<>(0.04f, frames);
        carAnim.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void runSimulation() {
        if (ACCIDENTS.isEmpty()) {
            ui.showStatus("Ni nesreč!", Color.RED);
            return;
        }
        if (VEHICLES.isEmpty()) {
            ui.showStatus("Ni vozil!", Color.RED);
            return;
        }
        backendPaths.clear();
        showPaths = false;


        int successCount = 0;
        int failCount = 0;

        for (Accident accident : ACCIDENTS) {
            String requiredVehicleType = mapAccidentToVehicleType(accident.typeOfAccident);
            Vehicle nearest = findNearestAvailableVehicle(accident, requiredVehicleType);

            if (nearest == null) {
                failCount++;
                continue;
            }

            nearest.isAssigned = true;

            Gdx.app.log("SIM", "Nesreča " + accident.id + " (" + accident.typeOfAccident +
                ") → Vozilo " + nearest.id);

            fetchRouteAndStartVehicle(nearest, accident);
            successCount++;
        }

        if (successCount > 0 && failCount == 0) {
            ui.showStatus("Poslanih " + successCount + " vozil!", Color.GREEN);
        } else if (successCount > 0 && failCount > 0) {
            ui.showStatus("Poslanih " + successCount + ", preskočenih " + failCount, Color.YELLOW);
        } else {
            ui.showStatus("Ni ustreznih vozil!", Color.RED);
        }
    }
    private Vehicle findNearestAvailableVehicle(Accident accident, String requiredType) {
        Vehicle nearest = null;
        double minDist = Double.MAX_VALUE;

        Gdx.app.log("SIM", "Iščem vozilo tipa '" + requiredType + "' za nesrečo " + accident.id);

        for (Vehicle v : VEHICLES) {
            if (v.type == null || !v.type.equalsIgnoreCase(requiredType)) {
                continue;
            }
            if (v.isAssigned) {
                continue;
            }
            if (v.locationStart == null) {
                continue;
            }
            double dist = haversineKm(
                accident.geolocation.lat, accident.geolocation.lng,
                v.locationStart.lat, v.locationStart.lng
            );

            if (dist < minDist) {
                minDist = dist;
                nearest = v;
            }
        }

        if (nearest != null) {
            Gdx.app.log("SIM", "✓ Najdeno: vozilo " + nearest.id +
                " (" + nearest.type + ") na razdalji " +
                String.format("%.2f km", minDist));
        } else {
            Gdx.app.log("SIM", "✗ Ni vozila tipa '" + requiredType + "' za nesrečo " + accident.id);
        }

        return nearest;
    }

    private void fetchRouteAndStartVehicle(Vehicle vehicle, Accident accident) {
        fetchRouteAndStartInternal(vehicle, accident, false);
    }

    private void fetchRouteAndStart(Vehicle vehicle, Accident accident) {
        fetchRouteAndStartInternal(vehicle, accident, true);
    }

    private void fetchRouteAndStartInternal(Vehicle vehicle, Accident accident, boolean withUiStatus) {
        vehicle.isMoving = false;

        double startLat = vehicle.locationStart.lat;
        double startLng = vehicle.locationStart.lng;
        double endLat = accident.geolocation.lat;
        double endLng = accident.geolocation.lng;

        vehicle.locationEnd = new Geolocation(endLat, endLng);

        if (withUiStatus) {
            Gdx.app.log("SIM", "Fetching route: " + startLat + "," + startLng + " -> " + endLat + "," + endLng);
        } else {
            Gdx.app.log("SIM", "Fetching route for vehicle " + vehicle.id + ": " +
                startLat + "," + startLng + " -> " + endLat + "," + endLng);
        }

        java.util.List<TrafficPoint> trafficPoints =
            (TRAFFIC_POINTS == null) ? java.util.Collections.emptyList() : TRAFFIC_POINTS;

        backendService.fetchORSRoute(
            startLat, startLng,
            endLat, endLng,
            beginTile,
            trafficPoints,
            new BackendService.VehiclePathCallback() {
                @Override
                public void onSuccess(ArrayList<Vector2> pts) {
                    if (withUiStatus) {
                        Gdx.app.log("SIM", "Route received with " + (pts == null ? 0 : pts.size()) + " points");
                    }

                    if (pts == null || pts.size() < 2) {
                        if (withUiStatus) ui.showStatus("Pot prekratka!", Color.RED);
                        return;
                    }
                    if (withUiStatus) backendPaths.clear();
                    backendPaths.add(new ArrayList<>(pts));
                    showPaths = true;

                    vehicle.pathPoints = pts;
                    vehicle.currentPos.set(pts.get(0));
                    vehicle.segIndex = 0;
                    vehicle.travelInSeg = 0f;
                    vehicle.isMoving = true;

                    vehicle.assignedToAccident = true;
                    vehicle.animTime = 0f;

                    if (withUiStatus) ui.showStatus("Vozilo se premika!", Color.GREEN);
                }

                @Override
                public void onError(Throwable t) {
                    String msg = (t == null || t.getMessage() == null) ? "neznana napaka" : t.getMessage();

                    if (withUiStatus) {
                        Gdx.app.error("SIM", "Route error: " + msg);
                        ui.showStatus("Napaka: " + msg, Color.RED);
                    } else {
                        Gdx.app.error("SIM", "Route error for " + vehicle.id + ": " + msg);
                    }

                    generateVehiclePathFallback(vehicle);

                    if (withUiStatus) backendPaths.clear();
                    backendPaths.add(new ArrayList<>(vehicle.pathPoints));
                    showPaths = true;

                    vehicle.isMoving = true;

                    if (withUiStatus) ui.showStatus("Fallback pot (ravna črta)", Color.ORANGE);
                }
            }
        );
    }


    private String mapAccidentToVehicleType(AccidentType t) {
        if (t == null) return "policijska";
        switch (t) {
            case KRIMINAL:
            case PROMETNA:
                return "Police";
            case ZDRAVSTVENI_PRIMER:
                return "Ambulance";
            case NARAVNA_NESRECA:
                return "Fire";
            default:
                return "Police";
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);

        float dt = Gdx.graphics.getDeltaTime();

        handleKeyboard();
        clampCamera();
        camera.update();

        updateCar(dt);
        updateVehicles(dt);

        viewport.apply();

        drawTiles();
        drawPaths();
        drawCar();
        drawStations();
        drawTraffic();
        drawVehicles(dt);
        drawLoadingOverlay();

        ui.actAndDraw();

        // samo če ni izbran marker, preverjaj hover za traffic points
        if (selectedMarker == null) {
            TrafficPoint hoverTrafficPoint = getTrafficPointAtScreen(Gdx.input.getX(), Gdx.input.getY());
            if (hoverTrafficPoint != null) {
                if (hoverTrafficPoint != lastHoveredTrafficPoint) {
                    lastHoveredTrafficPoint = hoverTrafficPoint;
                    if (hoverTrafficPoint.image == null && hoverTrafficPoint.imageBase64 != null) {
                        hoverTrafficPoint.loadImageFromBase64();
                    } else if (hoverTrafficPoint.image == null && hoverTrafficPoint.imageBase64 == null) {
                        backendService.fetchTrafficPointById(hoverTrafficPoint.id, new BackendService.SingleTrafficCallback() {
                            @Override
                            public void onSuccess(TrafficPoint loadedPoint) {
                                hoverTrafficPoint.image = loadedPoint.image;
                                hoverTrafficPoint.imageBase64 = loadedPoint.imageBase64;
                                showTrafficPointInfo(hoverTrafficPoint);
                            }
                            @Override
                            public void onError(Throwable t) {
                                Gdx.app.error("Backend", "Failed to load traffic point details", t);
                                showTrafficPointInfo(hoverTrafficPoint);
                            }
                        });
                    } else {
                        showTrafficPointInfo(hoverTrafficPoint);
                    }
                }
            } else {
                if (lastHoveredTrafficPoint != null) {
                    lastHoveredTrafficPoint = null;
                    ui.hideInfo();
                }
            }
        }
    }

    private void updateVehicles(float dt) {
        for (Vehicle vehicle : VEHICLES) {
            vehicle.update(dt);
        }
    }

    private void drawVehicles(float dt) {
        renderer.drawVehicles(spriteBatch, camera, VEHICLES, dt);
    }


    private void zoomTowardsMouse(float amountY) {
        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmp, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        float beforeX = tmp.x;
        float beforeY = tmp.y;

        if (amountY > 0) camera.zoom *= WHEEL_ZOOM_FACTOR;
        else camera.zoom /= WHEEL_ZOOM_FACTOR;

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);

        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmp, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        float afterX = tmp.x;
        float afterY = tmp.y;

        camera.position.add(beforeX - afterX, beforeY - afterY, 0);
    }

    private void drawTiles() {
        renderer.drawTiles(spriteBatch, camera, tileZone);
    }

    private void drawTraffic(){
        renderer.drawTrafficPoints(spriteBatch, camera, TRAFFIC_POINTS, beginTile, true);
    }

    private void drawCar() {
        renderer.drawCar(spriteBatch, camera, carAnim, carVisible, carStateTime, carSpriteW, carSpriteH, carConstantScreenSize, carPos, carRotationDeg);
    }

    private void drawPaths() {
        renderer.drawPaths(shapeRenderer, camera, showPaths, backendPaths);
    }

    private void drawStations() {
        renderer.drawStations(spriteBatch, camera, MARKERS, beginTile, ui.isPolicijaEnabled(), ui.isGasilciEnabled(), ui.isBolnicaEnabled());
    }

    private void drawLoadingOverlay() {
        renderer.drawLoadingOverlay(spriteBatch, camera, font, tileZone, viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    private void startCarAlongPath() {
        if (pathPoints.size() < 2) return;

        carMoving = true;
        carVisible = true;

        carSegIndex = 0;
        carTravelInSeg = 0f;
        carStateTime = 0f;

        carPos.set(pathPoints.get(0));

        Vector2 a = pathPoints.get(0);
        Vector2 b = pathPoints.get(1);
        carRotationDeg = MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees;
    }

    private float computePathLengthPx() {
        return logic.computePathLengthPx(pathPoints);
    }

    private void updateCar(float dt) {
        if (carVisible) {
            carStateTime += dt;
        }

        if (!carMoving || pathPoints.size() < 2) return;

        float move = carSpeed * dt;

        while (move > 0 && carSegIndex < pathPoints.size() - 1) {
            Vector2 a = pathPoints.get(carSegIndex);
            Vector2 b = pathPoints.get(carSegIndex + 1);

            float segLen = a.dst(b);
            float remaining = segLen - carTravelInSeg;

            if (segLen < 0.0001f) {
                carSegIndex++;
                carTravelInSeg = 0f;
                continue;
            }

            if (move < remaining) {
                carTravelInSeg += move;
                float t = carTravelInSeg / segLen;

                carPos.set(a).lerp(b, t);
                carRotationDeg = MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees;

                move = 0;
            } else {
                move -= remaining;
                carSegIndex++;
                carTravelInSeg = 0f;

                if (carSegIndex >= pathPoints.size() - 1) {
                    carPos.set(pathPoints.get(pathPoints.size() - 1));
                    carMoving = false;
                    break;
                }
            }
        }
    }

    private void clampCamera() {
        float halfW = viewport.getWorldWidth() * camera.zoom / 2f;
        float halfH = viewport.getWorldHeight() * camera.zoom / 2f;

        if (halfW * 2 >= Constants.MAP_WIDTH) {
            camera.position.x = Constants.MAP_WIDTH / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, halfW, Constants.MAP_WIDTH - halfW);
        }

        if (halfH * 2 >= Constants.MAP_HEIGHT) {
            camera.position.y = Constants.MAP_HEIGHT / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, halfH, Constants.MAP_HEIGHT - halfH);
        }
    }

    private void handleKeyboard() {
        float speed = 800 * camera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A))
            camera.translate(-speed, 0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D))
            camera.translate(speed, 0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S))
            camera.translate(0, -speed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W))
            camera.translate(0, speed, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom *= 1.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom /= 1.02f;

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            camera.zoom = 1.0f;
            camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);

            pathPoints.clear();
            showPaths = false;

            carMoving = false;
            carVisible = false;
        }
    }

    private void findNearestStations() {
        Marker nesreca = null;

        for (Marker m : MARKERS) {
            if (m.type == MarkerType.NESRECA) {
                nesreca = m;
                break;
            }
        }
        if (nesreca == null) return;

        ArrayList<Marker> postaje = new ArrayList<>();
        for (Marker m : MARKERS) {
            if (m.type == MarkerType.POSTAJA) postaje.add(m);
        }

        if (postaje.isEmpty()) return;

        ArrayList<Double> razdalje = new ArrayList<>();
        for (Marker p : postaje) {
            double d = haversineKm(
                nesreca.lokacija.lat, nesreca.lokacija.lng,
                p.lokacija.lat, p.lokacija.lng
            );
            razdalje.add(d);
        }

        int najblizjiIndeks = 0;
        double minRazdalja = razdalje.get(0);
        for (int i = 1; i < razdalje.size(); i++) {
            if (razdalje.get(i) < minRazdalja) {
                minRazdalja = razdalje.get(i);
                najblizjiIndeks = i;
            }
        }

        Marker najblizja = postaje.get(najblizjiIndeks);

        generatePath(najblizja, nesreca);
        showPath = true;

        float len = computePathLengthPx();
        if (len > 1f) {
            carSpeed = len / CAR_TRAVEL_SECONDS;
            carSpeed = MathUtils.clamp(carSpeed, CAR_MIN_SPEED, CAR_MAX_SPEED);
        } else {
            carSpeed = 1400f;
        }

        startCarAlongPath();
    }

    private void generatePath(Marker from, Marker to) {
        Vector2 startPos = MapRasterTiles.getPixelPosition(
            from.lokacija.lat, from.lokacija.lng,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        Vector2 endPos = MapRasterTiles.getPixelPosition(
            to.lokacija.lat, to.lokacija.lng,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        logic.generatePath(pathPoints, startPos, endPos);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        return logic.haversineKm(lat1, lon1, lat2, lon2);
    }

    private Marker getMarkerAtScreen(float screenX, float screenY) {
        tmp.set(screenX, screenY, 0);
        camera.unproject(tmp, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        Vector2 clickPos = new Vector2(tmp.x, tmp.y);

        float clickRadius = 800f * camera.zoom;

        for (Marker m : MARKERS) {
            Vector2 markerPos = MapRasterTiles.getPixelPosition(
                m.lokacija.lat, m.lokacija.lng,
                MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                beginTile.x, beginTile.y, Constants.MAP_HEIGHT
            );

            if (clickPos.dst(markerPos) <= clickRadius) return m;
        }
        return null;
    }

    private TrafficPoint getTrafficPointAtScreen(float screenX, float screenY) {
        tmp.set(screenX, screenY, 0);
        camera.unproject(tmp, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        Vector2 clickPos = new Vector2(tmp.x, tmp.y);

        float clickRadius = 800f * camera.zoom;

        for (TrafficPoint tp : TRAFFIC_POINTS) {
            Vector2 trafficPos = MapRasterTiles.getPixelPosition(
                tp.geolocation.lat, tp.geolocation.lng,
                MapRasterTiles.TILE_SIZE, Constants.ZOOM,
                beginTile.x, beginTile.y, Constants.MAP_HEIGHT
            );

            if (clickPos.dst(trafficPos) <= clickRadius) return tp;
        }
        return null;
    }

    private void showTrafficPointInfo(TrafficPoint trafficPoint) {
        if (trafficPoint == null) {
            ui.hideInfo();
            return;
        }

        Vector2 trafficPos = MapRasterTiles.getPixelPosition(
            trafficPoint.geolocation.lat,
            trafficPoint.geolocation.lng,
            MapRasterTiles.TILE_SIZE,
            Constants.ZOOM,
            beginTile.x,
            beginTile.y,
            Constants.MAP_HEIGHT
        );

        ui.showTrafficPointInfo(trafficPoint, camera, viewport, trafficPos);
    }

    private void showMarkerInfo(Marker marker) {
        if (marker == null || marker.station == null) {
            ui.hideInfo();
            return;
        }

        Vector2 markerPos = MapRasterTiles.getPixelPosition(
            marker.lokacija.lat,
            marker.lokacija.lng,
            MapRasterTiles.TILE_SIZE,
            Constants.ZOOM,
            beginTile.x,
            beginTile.y,
            Constants.MAP_HEIGHT
        );

        ui.showMarkerInfo(marker, camera, viewport, markerPos);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        ui.resize(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();

        for (Texture t : carFrameTextures) t.dispose();
        carFrameTextures.clear();

        ui.dispose();
        if (assetManager != null) assetManager.dispose();
    }

    @Override public boolean touchDown(float x, float y, int pointer, int button) { return false; }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (ui.hasUIFocus()) {
            return false;
        }
        if (ui.isClickOnUI(x, y)) {
            return false;
        }
        Marker clickedMarker = getMarkerAtScreen(x, y);
        if (clickedMarker != null && clickedMarker.type == MarkerType.POSTAJA) {
            if (selectedMarker == clickedMarker) {
                selectedMarker = null;
                ui.hideInfo();
            } else {
                selectedMarker = clickedMarker;
                showMarkerInfo(selectedMarker);
            }
            return true;
        } else {
            selectedMarker = null;
            ui.hideInfo();
            return false;
        }
    }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float velocityX, float velocityY, int button) { return false; }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        float scale = viewport.getWorldWidth() / viewport.getScreenWidth();
        camera.translate(-deltaX * scale * camera.zoom, deltaY * scale * camera.zoom);
        return true;
    }

    @Override public boolean panStop(float x, float y, int pointer, int button) { return false; }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance) camera.zoom *= 1.02f;
        else camera.zoom /= 1.02f;

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
    @Override public void pinchStop() { }

    private void generateVehiclePathFallback(Vehicle vehicle) {
        Vector2 startPos = MapRasterTiles.getPixelPosition(
            vehicle.locationStart.lat, vehicle.locationStart.lng,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        Vector2 endPos = MapRasterTiles.getPixelPosition(
            vehicle.locationEnd.lat, vehicle.locationEnd.lng,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        vehicle.pathPoints.clear();
        logic.generatePath(vehicle.pathPoints, startPos, endPos);

        if (!vehicle.pathPoints.isEmpty()) {
            vehicle.currentPos.set(vehicle.pathPoints.get(0));
            vehicle.segIndex = 0;
            vehicle.travelInSeg = 0f;
            vehicle.isMoving = true;
        }
    }
}
