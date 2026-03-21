package Entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import ImageManager.Animation;

public class PowerUp {

    public enum Type { SPEED_BOOST, TIMER_FREEZE }

    private static final int WIDTH  = 36;
    private static final int HEIGHT = 36;

    private double x, y, baseY;
    private double bobTime;
    private Type type;

    private boolean collected;
    private boolean fullyCollected;
    private int collectTimer;
    private static final int COLLECT_FRAMES = 40;

    private Animation animation;

    // Glow pulse
    private double glowTime;

    public PowerUp(double x, double y, Type type, ArrayList<BufferedImage> frames) {
        this.x      = x;
        this.y      = y;
        this.baseY  = y;
        this.type   = type;
        this.bobTime  = Math.random() * Math.PI * 2;
        this.glowTime = Math.random() * Math.PI * 2;

        if (frames != null && !frames.isEmpty()) {
            animation = new Animation();
            for (BufferedImage f : frames) {
                animation.addFrame(f, 80);
            }
        }
    }

    public void update() {
        if (fullyCollected) return;

        if (!collected) {
            bobTime  += 0.06;
            glowTime += 0.08;
            y = baseY + Math.sin(bobTime) * 6;
            if (animation != null) animation.update(20);
        } else {
            collectTimer++;
            if (collectTimer >= COLLECT_FRAMES) fullyCollected = true;
        }
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        if (fullyCollected) return;

        int drawX = (int) x - camX;
        int drawY = (int) y - camY;

        float alpha = collected
            ? Math.max(0f, 1f - (collectTimer / (float) COLLECT_FRAMES))
            : 1f;

        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Pulsing outer glow ring
        int glowPulse = (int) (40 + 30 * Math.sin(glowTime));
        Color glowColor = (type == Type.SPEED_BOOST)
            ? new Color(80, 200, 255, glowPulse)
            : new Color(255, 215, 60, glowPulse);
        g2.setColor(glowColor);
        g2.fillOval(drawX - 6, drawY - 6, WIDTH + 12, HEIGHT + 12);

        // Solid inner ring
        Color ringColor = (type == Type.SPEED_BOOST)
            ? new Color(60, 180, 255, 160)
            : new Color(255, 200, 40, 160);
        g2.setColor(ringColor);
        g2.fillOval(drawX - 2, drawY - 2, WIDTH + 4, HEIGHT + 4);

        // Star sprite (or fallback circle)
        if (animation != null) {
            g2.drawImage(animation.getImage(), drawX, drawY, WIDTH, HEIGHT, null);
        } else {
            // Fallback: simple colored star substitute
            g2.setColor(type == Type.SPEED_BOOST ? new Color(100, 220, 255) : new Color(255, 230, 80));
            g2.fillOval(drawX + 4, drawY + 4, WIDTH - 8, HEIGHT - 8);
        }

        g2.setComposite(oldComp);

        // Label underneath the power-up when not yet collected
        if (!collected) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            String label = (type == Type.SPEED_BOOST) ? "SPEED" : "FREEZE";
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int lx = drawX + (WIDTH - fm.stringWidth(label)) / 2;
            int ly = drawY + HEIGHT + 10;
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(lx - 3, ly - 9, fm.stringWidth(label) + 6, 12, 4, 4);
            g2.setColor(type == Type.SPEED_BOOST ? new Color(100, 220, 255) : new Color(255, 230, 80));
            g2.drawString(label, lx, ly);
            g2.setComposite(oldComp);
        }
    }

    public void collect() {
        if (!collected) {
            collected     = true;
            collectTimer  = 0;
        }
    }

    public boolean isCollected()     { return collected; }
    public boolean isFullyCollected(){ return fullyCollected; }
    public Type    getType()         { return type; }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }
}
