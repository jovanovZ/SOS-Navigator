package si.um.feri.navigator.OOP;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import si.um.feri.navigator.utils.Geolocation;

public class Vehicle {
    public String id;
    public String type;
    public float acceleration;
    public Geolocation locationStart;
    public Geolocation locationEnd; // Lahko je null, ko je vozilo prišlo do cilja

    // Pot za premikanje (iz Geoapify ali NavigationLogic)
    public ArrayList<Vector2> pathPoints = new ArrayList<>();

    // Za animacijo po poti (segmentno premikanje, kot car)
    public Vector2 currentPos = new Vector2();
    public float speed = 50f; // Pikseli na sekundo
    public float rotationDeg = 0f;

    // Za segmentno premikanje
    public int segIndex = 0;
    public float travelInSeg = 0f;
    public boolean isMoving = true;
    public boolean isAssigned = false;
    public boolean assignedToAccident = false;
    public float animTime = 0f;
    public Texture icon;

    // ✨ PRAZEN KONSTRUKTOR - POTREBEN ZA USTVARJANJE NOVIH VOZIL V SIMULACIJI ✨
    public Vehicle() {
    }

    // Konstruktor s parametri - uporablja se pri nalaganju vozil iz baze
    public Vehicle(String id, String type, float acceleration,
                   double startLng, double startLat,
                   double endLng, double endLat) {
        this.id = id;
        this.type = type;
        this.acceleration = acceleration;
        this.locationStart = new Geolocation(startLat, startLng);
        this.locationEnd = new Geolocation(endLat, endLng);
    }

    /**
     * Posodobi pozicijo vozila po poti
     * @param dt Delta time (sekunde)
     */
    public void update(float dt) {
        animTime += dt;
        if (!isMoving || pathPoints.size() < 2) return;

        if (segIndex >= pathPoints.size() - 1) {
            currentPos.set(pathPoints.get(pathPoints.size() - 1));

            if (locationEnd != null) {
                locationStart = locationEnd;
                locationEnd = null;
            }

            isMoving = false;
            return;
        }

        float move = speed * dt;

        while (move > 0 && segIndex < pathPoints.size() - 1) {
            Vector2 a = pathPoints.get(segIndex);
            Vector2 b = pathPoints.get(segIndex + 1);

            float segLen = a.dst(b);
            float remaining = segLen - travelInSeg;

            if (segLen < 0.0001f) {
                segIndex++;
                travelInSeg = 0f;
                continue;
            }

            if (move < remaining) {
                travelInSeg += move;
                float t = travelInSeg / segLen;

                currentPos.set(a).lerp(b, t);
                rotationDeg = MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees;

                move = 0;
            } else {
                move -= remaining;
                segIndex++;
                travelInSeg = 0f;

                if (segIndex >= pathPoints.size() - 1) {
                    currentPos.set(pathPoints.get(pathPoints.size() - 1));

                    if (locationEnd != null) {
                        locationStart = locationEnd;
                        locationEnd = null;
                    }
                    isMoving = false;
                    break;
                }
            }
        }
    }
}
