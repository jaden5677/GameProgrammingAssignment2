package Entities;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class Platform {
    private int x, y, width, height;
    private boolean isGround;

    private static final Color GRASS_TOP = new Color(76, 153, 0);
    private static final Color DIRT = new Color(139, 90, 43);
    private static final Color DIRT_DARK = new Color(101, 67, 33);
    private static final Color GROUND_GRASS = new Color(34, 139, 34);

    public Platform(int x, int y, int width, int height, boolean isGround) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isGround = isGround;
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        int drawX = x - camX;
        int drawY = y - camY;

        if (isGround) {
            g2.setColor(GROUND_GRASS);
            g2.fillRect(drawX, drawY, width, 8);
            GradientPaint gp = new GradientPaint(drawX, drawY + 8, DIRT, drawX, drawY + height, DIRT_DARK);
            g2.setPaint(gp);
            g2.fillRect(drawX, drawY + 8, width, height - 8);
        } else {
            GradientPaint gp = new GradientPaint(
                drawX, drawY, new Color(160, 140, 120),
                drawX, drawY + height, new Color(120, 100, 80));
            g2.setPaint(gp);
            g2.fillRoundRect(drawX, drawY, width, height, 6, 6);

            g2.setColor(GRASS_TOP);
            g2.fillRoundRect(drawX, drawY, width, 8, 6, 6);
            g2.fillRect(drawX + 3, drawY + 4, width - 6, 4);

            g2.setColor(new Color(80, 60, 40));
            g2.drawRoundRect(drawX, drawY, width, height, 6, 6);
        }
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public boolean isGround() { return isGround; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
