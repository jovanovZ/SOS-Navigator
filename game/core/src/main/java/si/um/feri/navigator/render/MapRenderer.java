package si.um.feri.navigator.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.MarkerType;
import si.um.feri.navigator.utils.Constants;
import si.um.feri.navigator.utils.MapRasterTiles;
import si.um.feri.navigator.utils.ZoomXY;

public class MapRenderer {

    public void drawTiles(SpriteBatch spriteBatch, OrthographicCamera camera, ZoomXY[] tileZone) {
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

    public void drawStations(
        SpriteBatch spriteBatch,
        OrthographicCamera camera,
        List<Marker> markers,
        ZoomXY beginTile,
        boolean showPolicija,
        boolean showGasilci,
        boolean showBolnice
    ) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float iconSize = 800f * camera.zoom;

        for (Marker marker : markers) {

            // NESREČA – vedno prikaži
            if (marker.type == MarkerType.NESRECA) {
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
                continue;
            }

            // POSTAJE – filtriranje po tipu
            if (marker.type == MarkerType.POSTAJA && marker.station != null) {
                switch (marker.station.type) {
                    case POLICIJSKA:
                        if (!showPolicija) continue;
                        break;
                    case GASILSKA:
                        if (!showGasilci) continue;
                        break;
                    case BOLNICA:
                        if (!showBolnice) continue;
                        break;
                }
            }

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


    public void drawPaths(ShapeRenderer shapeRenderer, OrthographicCamera camera, boolean showPaths, ArrayList<ArrayList<Vector2>> backendPaths) {

        if (!showPaths || backendPaths.isEmpty()) return;

        shapeRenderer.setProjectionMatrix(camera.combined);

        float lineWidth = 150f * camera.zoom;
        float borderWidth = 200f * camera.zoom;

        for (ArrayList<Vector2> path : backendPaths) {
            if (path.size() < 2) continue;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < path.size() - 1; i++) {
                Vector2 p1 = path.get(i);
                Vector2 p2 = path.get(i + 1);
                shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, borderWidth);
            }
            for (Vector2 p : path) shapeRenderer.circle(p.x, p.y, borderWidth / 2f);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.GREEN);
            for (int i = 0; i < path.size() - 1; i++) {
                Vector2 p1 = path.get(i);
                Vector2 p2 = path.get(i + 1);
                shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, lineWidth);
            }
            for (Vector2 p : path) shapeRenderer.circle(p.x, p.y, lineWidth / 2f);
            shapeRenderer.end();
        }
    }

    public void drawCar(SpriteBatch spriteBatch, OrthographicCamera camera, Animation<TextureRegion> carAnim, boolean carVisible, float carStateTime, float carSpriteW, float carSpriteH, boolean carConstantScreenSize, Vector2 carPos, float carRotationDeg) {

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

        spriteBatch.draw(frame,
            x, y,
            w / 2f, h / 2f,
            w, h,
            1f, 1f,
            carRotationDeg
        );

        spriteBatch.end();
    }

    public void drawLoadingOverlay(SpriteBatch spriteBatch, OrthographicCamera camera, BitmapFont font, ZoomXY[] tileZone, float viewportWorldWidth, float viewportWorldHeight) {

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

        float left = camera.position.x - (viewportWorldWidth * camera.zoom) / 2f + 500 * camera.zoom;
        float top = camera.position.y + (viewportWorldHeight * camera.zoom) / 2f - 500 * camera.zoom;

        font.draw(spriteBatch, "Loading: " + loaded + "/" + total, left, top);
        spriteBatch.end();
    }
}
