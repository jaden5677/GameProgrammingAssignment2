package Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import ImageManager.GrayScaleFX;
import ImageManager.DissapearFX;
import ImageManager.BrightnessFX;

public class Animal {
    private double x, y;
    private double baseY;
    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;

    private BufferedImage originalImage;
    private boolean collected;
    private boolean fullyCollected;

    // Collection animation timer
    private int collectTimer;
    private static final int COLLECT_DURATION = 60;

    // Bobbing animation
    private double bobTime;
    private static final double BOB_SPEED = 0.05;
    private static final double BOB_AMPLITUDE = 5;

    // Image effects for collection sequence
    private GrayScaleFX grayScaleFX;
    private DissapearFX dissapearFX;
    private BrightnessFX brightnessFX;

    public Animal(double x, double y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.baseY = y;
        this.originalImage = image;
        this.collected = false;
        this.fullyCollected = false;
        this.collectTimer = 0;
        this.bobTime = Math.random() * Math.PI * 2;

        this.grayScaleFX = new GrayScaleFX();
        this.dissapearFX = new DissapearFX();
        this.brightnessFX = new BrightnessFX(0);
    }

    public void update() {
        if (fullyCollected) return;

        if (!collected) {
            bobTime += BOB_SPEED;
            y = baseY + Math.sin(bobTime) * BOB_AMPLITUDE;
        } else {
            collectTimer++;
            if (collectTimer >= COLLECT_DURATION) {
                fullyCollected = true;
            }
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        if (fullyCollected) return;

        int drawX = (int) x - camX;
        int drawY = (int) y - camY;

        BufferedImage img = originalImage;

        if (collected && img != null) {
            // Phase 1: Brightness flash (frames 0-14)
            if (collectTimer < 15) {
                int bright = (int) (100 * (1 - collectTimer / 15.0));
                brightnessFX.setBrightness(bright);
                img = brightnessFX.apply(img);
            }
            // Phase 2: Grayscale (frames 15-34)
            else if (collectTimer < 35) {
                img = grayScaleFX.apply(img);
            }
            // Phase 3: Grayscale + Fade out (frames 35-59)
            else {
                int alpha = (int) (255 * (1 - (collectTimer - 35.0) / (COLLECT_DURATION - 35.0)));
                dissapearFX.setAlpha(Math.max(0, alpha));
                img = grayScaleFX.apply(img);
                img = dissapearFX.apply(img);
            }
        }

        if (img != null) {
            g2.drawImage(img, drawX, drawY, WIDTH, HEIGHT, null);
        } else {
            // Fallback: draw colored circle
            if (!collected) {
                g2.setColor(new Color(255, 200, 50));
            } else {
                int alpha = Math.max(0, 255 - collectTimer * 4);
                g2.setColor(new Color(128, 128, 128, alpha));
            }
            g2.fillOval(drawX, drawY, WIDTH, HEIGHT);
            g2.setColor(new Color(200, 150, 30));
            g2.drawOval(drawX, drawY, WIDTH, HEIGHT);
        }
    }

    public void collect() {
        if (!collected) {
            collected = true;
            collectTimer = 0;
        }
    }

    public boolean isCollected() { return collected; }
    public boolean isFullyCollected() { return fullyCollected; }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
