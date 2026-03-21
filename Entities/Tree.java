package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tree {
    private int x, y;
    private BufferedImage image;
    private int width, height;

    public Tree(int x, int y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = (image != null) ? Math.min(image.getWidth(), 120) : 80;
        this.height = (image != null) ? Math.min(image.getHeight(), 150) : 120;
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        int drawX = x - camX;
        int drawY = y - camY;

        if (image != null) {
            g2.drawImage(image, drawX, drawY - height, width, height, null);
        } else {
            // Fallback: simple procedural tree
            g2.setColor(new Color(101, 67, 33));
            g2.fillRect(drawX + width / 2 - 8, drawY - 60, 16, 60);
            g2.setColor(new Color(34, 139, 34));
            g2.fillOval(drawX, drawY - 120, width, 70);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
