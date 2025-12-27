package si.um.feri.navigator.utils;

public class ZoomXY {
    public int zoom;
    public int x;
    public int y;

    public ZoomXY(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return zoom + "/" + x + "/" + y;
    }
}
