package si.um.feri.navigator.OOP;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.Base64;

import si.um.feri.navigator.utils.Geolocation;

public class TrafficPoint {
    public String id;
    public Geolocation geolocation;
    public String status;
    public int vehicleCount;
    public Image image = null;
    public String imageBase64 = null;

    public Texture icon;

    public TrafficPoint(String status, double lat, double lng, String id, int vehicleCount, String base64 ) {
        this.geolocation = new Geolocation(lat, lng);
        this.id = id;
        this.status = status;
        this.vehicleCount = vehicleCount;
        this.imageBase64 = base64;

    }


    public void loadImageFromBase64() {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            this.image = null;
            return;
        }

        if (this.image != null) {
            return;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

            Pixmap pixmap = new Pixmap(imageBytes, 0, imageBytes.length);

            Texture texture = new Texture(pixmap);

            this.image = new Image(texture);

            pixmap.dispose();
        } catch (Exception e) {
            Gdx.app.error("TrafficPoint", "Error converting base64 to image: " + e.getMessage());
            this.image = null;
        }
    }

}
