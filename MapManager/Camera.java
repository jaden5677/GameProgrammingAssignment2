package MapManager;

public class Camera {
    private double x, y;
    private int viewportWidth, viewportHeight;
    private int worldWidth, worldHeight;
    private double smoothing;

    public Camera(int viewportWidth, int viewportHeight, int worldWidth, int worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.smoothing = 0.1;
        this.x = 0;
        this.y = 0;
    }

    public void follow(double targetX, double targetY) {
        double targetCamX = targetX - viewportWidth / 2.0;
        double targetCamY = targetY - viewportHeight / 2.0;

        // Smooth lerp interpolation
        x += (targetCamX - x) * smoothing;
        y += (targetCamY - y) * smoothing;

        // Clamp to world bounds
        x = Math.max(0, Math.min(x, worldWidth - viewportWidth));
        y = Math.max(0, Math.min(y, worldHeight - viewportHeight));
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }
}
