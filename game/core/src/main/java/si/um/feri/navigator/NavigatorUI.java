package si.um.feri.navigator.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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

    private SelectBox<String> typeSelectBox;
    private TextField latField;
    private TextField lngField;
    private Label statusLabel;
    private Marker currentMarker;


    public interface StationDeleteListener {
        void onDelete(Marker marker);
    }
    private StationDeleteListener deleteListener;

    public void setDeleteListener(StationDeleteListener listener) {
        this.deleteListener = listener;
    }

    public interface StationUpdateListener {
        void onUpdate(Marker marker, String newType, double newLat, double newLng);
    }
    private StationUpdateListener updateListener;

    public void setUpdateListener(StationUpdateListener listener) {
        this.updateListener = listener;
    }

    public boolean isClickOnInfoTable(float screenX, float screenY) {
        if (infoTable == null || !infoTable.isVisible()) return false;
        Vector2 stageCoords = uiStage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return infoTable.hit(stageCoords.x - infoTable.getX(), stageCoords.y - infoTable.getY(), true) != null;
    }

    public void init(Skin skin, BitmapFont font, Runnable onSimulate) {
        this.skin = skin;
        this.font = font;

        uiStage = new Stage(new ScreenViewport());

        infoTable = new Table(skin);
        infoTable.setVisible(false);
        infoTable.setBackground(skin.newDrawable("white", Color.DARK_GRAY));
        uiStage.addActor(infoTable);

        TextButton button = new TextButton("Izvedi simulacijo", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onSimulate != null) onSimulate.run();
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

        currentMarker = marker;
        Station s = marker.station;

        infoTable.clear();
        infoTable.defaults().pad(4).left();

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Label title = new Label(" Uredi postajo", labelStyle);
        title.setFontScale(0.45f);
        title.setColor(Color.YELLOW);
        infoTable.add(title).colspan(2).center().padBottom(6).row();

        Label typeLabel = new Label("Tip:", labelStyle);
        typeLabel.setFontScale(0.4f);
        infoTable.add(typeLabel);

        typeSelectBox = new SelectBox<>(skin);
        typeSelectBox.setItems("Policijska", "Bolnica", "Gasilci");
        typeSelectBox.setSelected(s.type);
        infoTable.add(typeSelectBox).width(140).height(35).row();

        Label latLabel = new Label("Lat:", labelStyle);
        latLabel.setFontScale(0.4f);
        infoTable.add(latLabel);

        latField = new TextField(String.format("%.5f", s.geolocation.lat), skin);
        infoTable.add(latField).width(140).height(35).row();

        Label lngLabel = new Label("Lng:", labelStyle);
        lngLabel.setFontScale(0.4f);
        infoTable.add(lngLabel);

        lngField = new TextField(String.format("%.5f", s.geolocation.lng), skin);
        infoTable.add(lngField).width(140).height(35).row();

        Label permLabel = new Label("Stalna: " + (s.isPermanent ? "Da" : "Ne"), labelStyle);
        permLabel.setFontScale(0.35f);
        permLabel.setColor(Color.LIGHT_GRAY);
        infoTable.add(permLabel).colspan(2).center().row();

        statusLabel = new Label("", labelStyle);
        statusLabel.setFontScale(0.35f);
        infoTable.add(statusLabel).colspan(2).center().row();

        TextButton saveBtn = new TextButton("Shrani", skin);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                trySave();
            }
        });

        TextButton deleteBtn = new TextButton("Izbrisi", skin);
        deleteBtn.setColor(Color.RED);
        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (deleteListener != null && currentMarker != null) {
                    statusLabel.setText("Brisem...");
                    statusLabel.setColor(Color.YELLOW);
                    deleteListener.onDelete(currentMarker);
                }
            }
        });

        Table btnTable = new Table();
        btnTable.add(saveBtn).width(90).height(40).padRight(10);
        btnTable.add(deleteBtn).width(90).height(40);

        infoTable.add(btnTable).colspan(2).center().padTop(6).row();

        tmp.set(markerPosWorld.x, markerPosWorld.y, 0);
        camera.project(tmp, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        infoTable.pack();
        infoTable.setPosition(tmp.x - infoTable.getWidth() / 2f, tmp.y + 60);
        infoTable.setVisible(true);
    }

    private void trySave() {
        if (currentMarker == null || updateListener == null) return;

        try {
            String newType = typeSelectBox.getSelected();
            double newLat = Double.parseDouble(latField.getText().replace(",", "."));
            double newLng = Double.parseDouble(lngField.getText().replace(",", "."));

            if (newLat < -90 || newLat > 90 || newLng < -180 || newLng > 180) {
                statusLabel.setText("Napacne koordinate!");
                statusLabel.setColor(Color.RED);
                return;
            }

            updateListener.onUpdate(currentMarker, newType, newLat, newLng);

        } catch (NumberFormatException e) {
            statusLabel.setText("Vnesite stevilke!");
            statusLabel.setColor(Color.RED);
        }
    }

    public void showStatus(String msg, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setColor(color);
        }
    }
    public void showTrafficPointInfo(TrafficPoint trafficPoint, OrthographicCamera camera, Viewport viewport, Vector2 trafficPosWorld) {
        if (trafficPoint == null || infoTable == null) {
            hideInfo();
            return;
        }

        currentMarker = null;

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
    public boolean hasUIFocus() {
        return uiStage.getKeyboardFocus() != null || uiStage.getScrollFocus() != null;
    }

    public boolean isClickOnUI(float screenX, float screenY) {
        if (uiStage == null) return false;
        Vector2 stageCoords = uiStage.screenToStageCoordinates(new Vector2(screenX, screenY));
        return uiStage.hit(stageCoords.x, stageCoords.y, true) != null;
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
