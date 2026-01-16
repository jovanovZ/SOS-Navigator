package si.um.feri.navigator.logic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class NavigationLogic {

    public float computePathLengthPx(List<Vector2> pathPoints) {
        if (pathPoints.size() < 2) return 0f;
        float sum = 0f;
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            sum += pathPoints.get(i).dst(pathPoints.get(i + 1));
        }
        return sum;
    }

    public void generatePath(List<Vector2> pathPoints, Vector2 startPos, Vector2 endPos) {
        pathPoints.clear();

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

    public double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
