package si.um.feri.navigator.utils;

import com.google.gson.Gson;
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
import si.um.feri.navigator.utils.Keys;

public class BackendService {

    public interface MarkerCallback {
        void onSuccess(ArrayList<Marker> markers);
        void onError(Throwable t);
    }

    private final Texture hospitalIcon;
    private final Texture fireIcon;
    private final Texture policeIcon;

    public BackendService() {
        hospitalIcon = new Texture(Gdx.files.internal("icons/hospital.png"));
        fireIcon = new Texture(Gdx.files.internal("icons/firestation.png"));
        policeIcon = new Texture(Gdx.files.internal("icons/policestation.png"));
    }

    public void fetchMarkers(MarkerCallback callback) {
        new Thread(() -> {
            try {
                String uri = Keys.SERVER_URL + "/api/station/all";
                HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();
                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                String jsonString = res.body();
                JsonArray arr = JsonParser.parseString(jsonString).getAsJsonArray();

                ArrayList<Marker> markers = new ArrayList<>();

                for (int i = 0; i < arr.size(); i++) {
                    JsonObject obj = arr.get(i).getAsJsonObject();
                    //System.out.println(obj);
                    JsonArray coords = obj.getAsJsonObject("locationId")
                        .getAsJsonObject("geometry")
                        .getAsJsonArray("coordinates");

                    double lng = coords.get(1).getAsDouble();
                    double lat = coords.get(0).getAsDouble();

                    String typeStr = obj.get("typeOfStation").getAsString();
                    MarkerType type;
                    Texture icon;

                    switch(typeStr.toLowerCase()) {
                        case "bolnica":
                            type = MarkerType.BOLNICA;
                            icon = hospitalIcon;
                            break;
                        case "gasilci":
                            type = MarkerType.GASILSKA;
                            icon = fireIcon;
                            break;
                        case "policijska":
                            type = MarkerType.POLICIJSKA;
                            icon = policeIcon;
                            break;
                        default:
                            type = MarkerType.NESRECA;
                            icon = policeIcon;
                            break;
                    }
                    markers.add(new Marker(type, lat, lng, icon));
                }

                Gdx.app.postRunnable(() -> callback.onSuccess(markers));

            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        }).start();
    }
}
