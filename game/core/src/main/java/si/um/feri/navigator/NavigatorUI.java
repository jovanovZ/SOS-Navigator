package si.um.feri.navigator.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.navigator.OOP.Marker;
import si.um.feri.navigator.OOP.Station;
import si.um.feri.navigator.OOP.TrafficPoint;

public class NavigatorUI {

    private final Vector3 tmp = new Vector3();

    private Stage uiStage;
    private Skin skin;
    private BitmapFont font;

    private Table infoTable;

    private boolean policijaEnabled = true;
    private boolean gasilciEnabled = true;
    private boolean bolnicaEnabled = true;

    private TextButton btnPolicija;
    private TextButton btnGasilci;
    private TextButton btnBolnica;

    public void init(Skin skin, BitmapFont font, Runnable onFindNearest) {
        this.skin = skin;
        this.font = font;

        uiStage = new Stage(new ScreenViewport());

        infoTable = new Table(skin);
        infoTable.setVisible(false);
        infoTable.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
        uiStage.addActor(infoTable);

        TextButton button = new TextButton("Najdi 5 najblizjih", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onFindNearest != null) onFindNearest.run();
            }
        });

        Table actionTable = new Table();
        actionTable.setFillParent(true);
        actionTable.bottom().right();
        actionTable.pad(20);
        actionTable.add(button).width(260).height(70);
        uiStage.addActor(actionTable);

        btnPolicija = createToggleButton("Policija", policijaEnabled);
        btnGasilci = createToggleButton("Gasilci", gasilciEnabled);
        btnBolnica = createToggleButton("Bolnice", bolnicaEnabled);

        btnPolicija.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                policijaEnabled = !policijaEnabled;
                updateToggleText(btnPolicija, "Policija", policijaEnabled);
            }
        });

        btnGasilci.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gasilciEnabled = !gasilciEnabled;
                updateToggleText(btnGasilci, "Gasilci", gasilciEnabled);
            }
        });

        btnBolnica.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                bolnicaEnabled = !bolnicaEnabled;
                updateToggleText(btnBolnica, "Bolnice", bolnicaEnabled);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.left().bottom();
        uiStage.addActor(root);

        Table filterPanel = new Table(skin);
        filterPanel.setBackground(skin.newDrawable("white", new Color(0.12f, 0.12f, 0.14f, 0.95f)));
        filterPanel.pad(2f);

        float btnWidth = 280f;
        float btnHeight = 60f;

        filterPanel.add(btnPolicija).width(btnWidth).height(btnHeight).left().padBottom(5f).row();
        filterPanel.add(btnGasilci).width(btnWidth).height(btnHeight).left().padBottom(5f).row();
        filterPanel.add(btnBolnica).width(btnWidth).height(btnHeight).left();

        root.add(filterPanel).left().bottom().pad(20);
    }

    private TextButton createToggleButton(String name, boolean enabled) {
        String text = (enabled ? "[X]  " : "[  ]  ") + name;
        TextButton btn = new TextButton(text, skin);
        btn.getLabel().setAlignment(Align.left);
        return btn;
    }

    private void updateToggleText(TextButton btn, String name, boolean enabled) {
        String text = (enabled ? "[X]  " : "[  ]  ") + name;
        btn.setText(text);
    }

    public Stage getStage() {
        return uiStage;
    }

    public void actAndDraw() {
        if (uiStage == null) return;
        uiStage.act();
        uiStage.draw();
    }

    public void resize(int width, int height) {
        if (uiStage == null) return;
        uiStage.getViewport().update(width, height, true);
    }

    public void hideInfo() {
        if (infoTable != null) infoTable.setVisible(false);
    }

    public void showMarkerInfo(Marker marker, OrthographicCamera camera, Viewport viewport, Vector2 markerPosWorld) {
        if (marker == null || marker.station == null || infoTable == null) {
            hideInfo();
            return;
        }

        infoTable.clear();
        infoTable.defaults().pad(4).left();

        Station s = marker.station;

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Label typeLabel = new Label("Type: " + s.type, labelStyle);
        typeLabel.setFontScale(0.5f);
        infoTable.add(typeLabel).row();

        Label permLabel = new Label("Permanent: " + s.isPermanent, labelStyle);
        permLabel.setFontScale(0.5f);
        infoTable.add(permLabel).row();

        Label lonLabel = new Label("Lon: " + s.geolocation.lng, labelStyle);
        lonLabel.setFontScale(0.5f);
        infoTable.add(lonLabel).row();

        Label latLabel = new Label("Lat: " + s.geolocation.lat, labelStyle);
        latLabel.setFontScale(0.5f);
        infoTable.add(latLabel).row();

        tmp.set(markerPosWorld.x, markerPosWorld.y, 0);
        camera.project(tmp, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        infoTable.pack();
        infoTable.setPosition(tmp.x - infoTable.getWidth() / 2f, tmp.y + 60);

        infoTable.setVisible(true);
    }

    public void showTrafficPointInfo(TrafficPoint trafficPoint, OrthographicCamera camera, Viewport viewport, Vector2 trafficPosWorld) {
        if (trafficPoint == null || infoTable == null) {
            hideInfo();
            return;
        }

        infoTable.clear();
        infoTable.defaults().pad(4).left();

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Label statusLabel = new Label("Status: " + trafficPoint.status, labelStyle);
        statusLabel.setFontScale(0.5f);
        infoTable.add(statusLabel).row();

        Label vehicleCountLabel = new Label("Vehicle Count: " + trafficPoint.vehicleCount, labelStyle);
        vehicleCountLabel.setFontScale(0.5f);
        infoTable.add(vehicleCountLabel).row();

        /*Label idLabel = new Label("ID: " + trafficPoint.id, labelStyle);
        idLabel.setFontScale(0.5f);
        infoTable.add(idLabel).row();*/

        Label lonLabel = new Label("Lon: " + trafficPoint.geolocation.lng, labelStyle);
        lonLabel.setFontScale(0.5f);
        infoTable.add(lonLabel).row();

        Label latLabel = new Label("Lat: " + trafficPoint.geolocation.lat, labelStyle);
        latLabel.setFontScale(0.5f);
        infoTable.add(latLabel).row();

        if (trafficPoint.image != null) {
            infoTable.add(trafficPoint.image)
                .maxSize(200f, 75f)
                .padTop(8)
                .row();
        } else {
            Label loadingLabel = new Label("Loading image...", labelStyle);
            loadingLabel.setFontScale(0.4f);
            loadingLabel.setColor(Color.YELLOW);
            infoTable.add(loadingLabel).padTop(8).row();
        }


        tmp.set(trafficPosWorld.x, trafficPosWorld.y, 0);
        camera.project(tmp, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        infoTable.pack();
        infoTable.setPosition(tmp.x - infoTable.getWidth() / 2f, tmp.y + 60);

        infoTable.setVisible(true);
    }


    public boolean isPolicijaEnabled() {
        return policijaEnabled;
    }

    public boolean isGasilciEnabled() {
        return gasilciEnabled;
    }

    public boolean isBolnicaEnabled() {
        return bolnicaEnabled;
    }

    public void dispose() {
        if (uiStage != null) uiStage.dispose();
    }
}
