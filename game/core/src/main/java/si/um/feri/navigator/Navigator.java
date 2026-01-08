package si.um.feri.navigator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.MarkerType;
import si.um.feri.navigator.OOP.Path;
import si.um.feri.navigator.OOP.Station;
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

    private Stage uiStage;
    private Skin skin;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 tmp = new Vector3();

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

    private BackendService backendService;

    private Table infoTable;
    private boolean infoVisible = false;

    private final ArrayList<ArrayList<Vector2>> backendPaths = new ArrayList<>();



    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, camera);
        viewport.apply();

        camera.zoom = 1.0f;
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.update();

        uiStage = new Stage(new ScreenViewport());
        skin = createBasicSkin();

        infoTable = new Table(skin);
        infoTable.setVisible(false);
        infoTable.setBackground(skin.newDrawable("button-up", Color.DARK_GRAY));
        uiStage.addActor(infoTable);


        TextButton button = new TextButton("Najdi 5 najbližjih", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                findNearestStations();
                Gdx.app.log("TUKAJ", "TUKAJ");
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.bottom().right();
        table.pad(20);
        table.add(button).width(200).height(50);
        uiStage.addActor(table);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);

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


        // BACKEND KLIC
        backendService = new BackendService();
        backendService.fetchMarkers(new BackendService.MarkerCallback() {
            @Override
            public void onSuccess(ArrayList<Marker> markers) {
                MARKERS.addAll(markers);
            }

            @Override
            public void onError(Throwable t) {
                Gdx.app.error("Backend", "Failed to load markers", t);
            }
        });
        backendService.fetchPaths(new BackendService.PathCallback() {
            @Override
            public void onSuccess(ArrayList<Path> paths) {
                backendPaths.clear();

                for (Path p : paths) {
                    ArrayList<Vector2> polyline = new ArrayList<>();

                    for (Geolocation g : p.points) {
                        Vector2 px = MapRasterTiles.getPixelPosition(
                            g.lat,
                            g.lng,
                            MapRasterTiles.TILE_SIZE,
                            Constants.ZOOM,
                            beginTile.x,
                            beginTile.y,
                            Constants.MAP_HEIGHT
                        );
                        polyline.add(px);
                    }

                    backendPaths.add(polyline);
                }

                if (!backendPaths.isEmpty()) {
                    pathPoints.clear();
                    pathPoints.addAll(backendPaths.get(0));
                    showPaths = false;
                    //
                }
            }

            @Override
            public void onError(Throwable t) {
                Gdx.app.error("Backend", "Failed to fetch paths", t);
            }
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

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.5f);
        skin.add("default-font", buttonFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        skin.add("button-up", new Texture(pixmap));

        pixmap.setColor(0.4f, 0.4f, 0.4f, 1f);
        pixmap.fill();
        skin.add("button-over", new Texture(pixmap));

        pixmap.setColor(0.2f, 0.2f, 0.2f, 1f);
        pixmap.fill();
        skin.add("button-down", new Texture(pixmap));

        pixmap.dispose();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.newDrawable("button-up");
        buttonStyle.over = skin.newDrawable("button-over");
        buttonStyle.down = skin.newDrawable("button-down");
        buttonStyle.font = skin.getFont("default-font");
        buttonStyle.fontColor = Color.WHITE;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        skin.add("default", buttonStyle);
        return skin;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);

        float dt = Gdx.graphics.getDeltaTime();

        handleKeyboard();
        clampCamera();
        camera.update();

        updateCar(dt);

        viewport.apply();

        drawTiles();
        //drawPath();
        drawPaths();
        drawCar();
        drawMarkers();
        drawLoadingOverlay();

        uiStage.act();
        uiStage.draw();
        Marker hoverMarker = getMarkerAtScreen(Gdx.input.getX(), Gdx.input.getY());
        if (hoverMarker != null && hoverMarker.type == MarkerType.POSTAJA) {
            showMarkerInfo(hoverMarker);
        } else {
            infoTable.setVisible(false);
        }
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
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        int idx = 0;
        for (int row = 0; row < Constants.NUM_TILES_Y; row++) {
            for (int col = 0; col < Constants.NUM_TILES_X; col++) {
                ZoomXY txy = tileZone[idx++];
                Texture t = MapRasterTiles.getRasterTileCachedAsync(txy.zoom, txy.x, txy.y);
                if (t != null) {
                    float x = col * MapRasterTiles.TILE_SIZE;
                    float y = (Constants.NUM_TILES_Y - 1 - row) * MapRasterTiles.TILE_SIZE;
                    spriteBatch.draw(t, x, y, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
                }
            }
        }
        spriteBatch.end();
    }

    private void drawCar() {
        if (carAnim == null) return;
        if (!carVisible) return;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();


        TextureRegion frame = carAnim.getKeyFrame(carStateTime, true);

        float w = carSpriteW;
        float h = carSpriteH;

        if (carConstantScreenSize) {
            w *= camera.zoom;
            h *= camera.zoom;
        }

        float x = carPos.x - w / 2f;
        float y = carPos.y - h / 2f;

        spriteBatch.draw(
            frame,
            x, y,
            w / 2f, h / 2f,
            w, h,
            1f, 1f,
            carRotationDeg
        );

        spriteBatch.end();
    }

    private void drawPath() {
        if (!showPath || pathPoints.size() < 2) return;

        shapeRenderer.setProjectionMatrix(camera.combined);

        float lineWidth = 150f * camera.zoom;
        float borderWidth = 200f * camera.zoom;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Vector2 p1 = pathPoints.get(i);
            Vector2 p2 = pathPoints.get(i + 1);
            shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, borderWidth);
        }
        for (Vector2 p : pathPoints) {
            shapeRenderer.circle(p.x, p.y, borderWidth / 2f);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Vector2 p1 = pathPoints.get(i);
            Vector2 p2 = pathPoints.get(i + 1);
            shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, lineWidth);
        }
        for (Vector2 p : pathPoints) {
            shapeRenderer.circle(p.x, p.y, lineWidth / 2f);
        }
        shapeRenderer.end();
    }


    private void drawPaths() {
        if (!showPaths || backendPaths.isEmpty()) return;

        shapeRenderer.setProjectionMatrix(camera.combined);

        float lineWidth = 150f * camera.zoom;
        float borderWidth = 200f * camera.zoom;

        for (ArrayList<Vector2> path : backendPaths) {
            if (path.size() < 2) continue;

            // BEL ROB
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < path.size() - 1; i++) {
                Vector2 p1 = path.get(i);
                Vector2 p2 = path.get(i + 1);
                shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, borderWidth);
            }
            for (Vector2 p : path) {
                shapeRenderer.circle(p.x, p.y, borderWidth / 2f);
            }
            shapeRenderer.end();

            // BARVNA POT
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GREEN);
            for (int i = 0; i < path.size() - 1; i++) {
                Vector2 p1 = path.get(i);
                Vector2 p2 = path.get(i + 1);
                shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, lineWidth);
            }
            for (Vector2 p : path) {
                shapeRenderer.circle(p.x, p.y, lineWidth / 2f);
            }
            shapeRenderer.end();
        }
    }


    private void drawLoadingOverlay() {
        int total = Constants.NUM_TILES_X * Constants.NUM_TILES_Y;

        int loaded = 0;
        for (ZoomXY txy : tileZone) {
            Texture t = MapRasterTiles.getRasterTileCachedAsync(txy.zoom, txy.x, txy.y);
            if (t != null) loaded++;
        }
        if (loaded >= total) return;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.setColor(Color.WHITE);

        float scale = camera.zoom * 40f;
        font.getData().setScale(scale);

        float left = camera.position.x - (viewport.getWorldWidth() * camera.zoom) / 2f + 500 * camera.zoom;
        float top = camera.position.y + (viewport.getWorldHeight() * camera.zoom) / 2f - 500 * camera.zoom;

        font.draw(spriteBatch, "Loading: " + loaded + "/" + total, left, top);
        spriteBatch.end();
    }

    private void drawMarkers() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float iconSize = 800f * camera.zoom;

        for (Marker marker : MARKERS) {
            Vector2 pos = MapRasterTiles.getPixelPosition(
                marker.lokacija.lat,
                marker.lokacija.lng,
                MapRasterTiles.TILE_SIZE,
                Constants.ZOOM,
                beginTile.x,
                beginTile.y,
                Constants.MAP_HEIGHT
            );

            if (marker.icon != null) {
                spriteBatch.draw(
                    marker.icon,
                    pos.x - iconSize / 2f,
                    pos.y - iconSize / 2f,
                    iconSize,
                    iconSize
                );
            }
        }

        spriteBatch.end();
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
        if (pathPoints.size() < 2) return 0f;
        float sum = 0f;
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            sum += pathPoints.get(i).dst(pathPoints.get(i + 1));
        }
        return sum;
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
        if (nesreca == null) {
            return;
        }
        Gdx.app.log("NESRECA", nesreca.toString());

        ArrayList<Marker> postaje = new ArrayList<>();
        for (Marker m : MARKERS) {
            if (m.type == MarkerType.POSTAJA) {
                postaje.add(m);
            }
        }

        if (postaje.isEmpty()) {
            return;
        }

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
        pathPoints.clear();

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

        int numPoints = 120;

        float dx = endPos.x - startPos.x;
        float dy = endPos.y - startPos.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 0.0001f) {
            pathPoints.add(new Vector2(startPos));
            pathPoints.add(new Vector2(endPos));
            return;
        }

        float perpX = -dy / length;
        float perpY = dx / length;

        float waveAmplitude = length * 0.08f;

        float seed1 = MathUtils.random(0f, 1000f);
        float seed2 = MathUtils.random(0f, 1000f);

        for (int i = 0; i <= numPoints; i++) {
            float t = (float) i / numPoints;

            float baseX = startPos.x + dx * t;
            float baseY = startPos.y + dy * t;

            float envelope = (float) Math.sin(t * Math.PI);

            float wave1 = (float) Math.sin(t * Math.PI * 4 + seed1) * waveAmplitude * 0.6f;
            float wave2 = (float) Math.sin(t * Math.PI * 7 + seed2) * waveAmplitude * 0.3f;
            float wave3 = (float) Math.sin(t * Math.PI * 13 + seed1 * 0.5f) * waveAmplitude * 0.15f;

            float totalOffset = (wave1 + wave2 + wave3) * envelope;
            float noise = MathUtils.random(-waveAmplitude * 0.05f, waveAmplitude * 0.05f) * envelope;
            totalOffset += noise;

            float finalX = baseX + perpX * totalOffset;
            float finalY = baseY + perpY * totalOffset;

            pathPoints.add(new Vector2(finalX, finalY));
        }
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
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

            if (clickPos.dst(markerPos) <= clickRadius) {
                return m;
            }
        }
        return null;
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        uiStage.dispose();
        skin.dispose();

        for (Texture t : carFrameTextures) {
            t.dispose();
        }
        for (Marker m : MARKERS) {
            if (m.icon != null) {
                m.icon.dispose();
            }
        }
        carFrameTextures.clear();
    }

    private void showMarkerInfo(Marker marker) {
        if (marker == null || marker.station == null) {
            infoTable.setVisible(false);
            infoVisible = false;
            return;
        }

        infoTable.clear();
        infoTable.defaults().pad(5);


        Station s = marker.station;

        infoTable.add("ID: " + s.id).row();
        infoTable.add("Type: " + s.type).row();
        infoTable.add("Permanent: " + s.isPermanent).row();
        infoTable.add("Long: " + s.geolocation.lng).row();
        infoTable.add("Lat: " + s.geolocation.lat).row();


        Vector2 markerPos = MapRasterTiles.getPixelPosition(
            marker.lokacija.lat, marker.lokacija.lng,
            MapRasterTiles.TILE_SIZE, Constants.ZOOM,
            beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );
        tmp.set(markerPos.x, markerPos.y, 0);
        camera.project(tmp, viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());

        infoTable.pack();
        infoTable.setPosition(tmp.x - infoTable.getWidth() / 2f, tmp.y + 50);
        infoTable.setVisible(true);
        infoVisible = true;
    }

    @Override public boolean touchDown(float x, float y, int pointer, int button) { return false; }
    @Override public boolean tap(float x, float y, int count, int button) { return false; }
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
}
