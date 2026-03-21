package Entities;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import ImageManager.AtlasLoader;
import ImageManager.Animation;

public class Player {
    private double x, y;
    private double vx, vy;

    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;

    private static final double GRAVITY = 0.6;
    private static final double JUMP_VELOCITY = -15;
    private static final double MOVE_SPEED = 5;
    private static final double MAX_FALL_SPEED = 15;

    private boolean onGround;
    private boolean facingRight;
    private boolean isJumping;

    // Sprite sheet animation - individual frames extracted from knight region
    private BufferedImage[] idleFrames;       // 4 idle animation frames
    private BufferedImage[] runFrames;        // 8 run animation frames
    private Animation starAnimation;          // StarActor animated frames
    private Animation explodeAnimation;       // CellExplode animated frames
    private boolean hasAtlasSprites;

    // Fallback animation
    private int animFrame;
    private int animTimer;

    private int animalsCollected;

    // Star effect timer when collecting
    private int collectEffectTimer;
    private static final int COLLECT_EFFECT_DURATION = 40;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.onGround = false;
        this.facingRight = true;
        this.isJumping = false;
        this.animFrame = 0;
        this.animTimer = 0;
        this.animalsCollected = 0;
        this.collectEffectTimer = 0;
        this.hasAtlasSprites = false;

        loadAtlasSprites();
    }

    private void loadAtlasSprites() {
        AtlasLoader atlas = new AtlasLoader("Entities/PlayerSprite.atlas");

        // Load the knight 256x256 region (it's a mini sprite sheet)
        AtlasLoader.AtlasRegion knightRegion = atlas.getRegion("knight");
        if (knightRegion != null && knightRegion.image != null) {
            BufferedImage knightSheet = knightRegion.image;

            // Extract IDLE frames from Row 1 (y=9..27): 4 knight frames at 32px intervals
            // Positions within knight region: x=9, 41, 73, 105
            int[][] idlePositions = {{9, 9, 13, 19}, {41, 10, 13, 18}, {73, 10, 13, 18}, {105, 10, 13, 18}};
            idleFrames = extractFrames(knightSheet, idlePositions);

            // Extract RUN frames from Row 3 (y=74..91): 8 frames
            int[][] runPositions = {
                {8, 74, 14, 18}, {41, 74, 13, 18}, {73, 74, 13, 18}, {105, 74, 13, 18},
                {136, 74, 14, 18}, {169, 74, 13, 18}, {201, 74, 13, 18}, {233, 74, 13, 18}
            };
            runFrames = extractFrames(knightSheet, runPositions);

            if (idleFrames.length > 0 || runFrames.length > 0) {
                hasAtlasSprites = true;
            }
        }

        // Load StarActor animation frames
        ArrayList<BufferedImage> starFrames = atlas.getFrameImages("StarActor");
        if (!starFrames.isEmpty()) {
            starAnimation = new Animation();
            for (BufferedImage frame : starFrames) {
                starAnimation.addFrame(frame, 80);
            }
        }

        // Load CellExplode animation frames
        ArrayList<BufferedImage> explodeFrames = atlas.getFrameImages("CellExplode");
        if (!explodeFrames.isEmpty()) {
            explodeAnimation = new Animation();
            for (BufferedImage frame : explodeFrames) {
                explodeAnimation.addFrame(frame, 60);
            }
        }
    }

    /** Extract sub-frames from a sprite sheet region. Each entry: {x, y, width, height} */
    private BufferedImage[] extractFrames(BufferedImage sheet, int[][] positions) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        for (int[] pos : positions) {
            int fx = pos[0], fy = pos[1], fw = pos[2], fh = pos[3];
            if (fx + fw <= sheet.getWidth() && fy + fh <= sheet.getHeight() && fw > 0 && fh > 0) {
                frames.add(sheet.getSubimage(fx, fy, fw, fh));
            }
        }
        return frames.toArray(new BufferedImage[0]);
    }

    public void update(boolean left, boolean right, boolean jump, ArrayList<Platform> platforms) {
        // Horizontal movement
        if (left) {
            vx = -MOVE_SPEED;
            facingRight = false;
        } else if (right) {
            vx = MOVE_SPEED;
            facingRight = true;
        } else {
            vx = 0;
        }

        // Jump
        if (jump && onGround) {
            vy = JUMP_VELOCITY;
            onGround = false;
            isJumping = true;
        }

        // Apply gravity
        vy += GRAVITY;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        // Move horizontally and resolve collisions
        x += vx;
        resolveHorizontalCollisions(platforms);

        // Move vertically and resolve collisions
        y += vy;
        onGround = false;
        resolveVerticalCollisions(platforms);

        // World bounds
        if (x < 0) x = 0;
        if (x + WIDTH > 4000) x = 4000 - WIDTH;

        // Fall off world -> respawn
        if (y > 1100) {
            respawn();
        }

        // Animation update
        animTimer++;
        if (animTimer >= 8) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 4;
        }

        // Update atlas animations
        if (starAnimation != null) {
            starAnimation.update(20); // ~20ms per game tick
        }
        if (explodeAnimation != null) {
            explodeAnimation.update(20);
        }

        // Collect effect countdown
        if (collectEffectTimer > 0) {
            collectEffectTimer--;
        }

        if (onGround) isJumping = false;
    }

    private void resolveHorizontalCollisions(ArrayList<Platform> platforms) {
        Rectangle2D.Double myBounds = getBounds();
        for (Platform p : platforms) {
            // Only solid horizontal collision with ground segments
            if (!p.isGround()) continue;
            Rectangle2D.Double pBounds = p.getBounds();
            if (myBounds.intersects(pBounds)) {
                if (vx > 0) {
                    x = p.getX() - WIDTH;
                } else if (vx < 0) {
                    x = p.getX() + p.getWidth();
                }
                vx = 0;
                myBounds = getBounds();
            }
        }
    }

    private void resolveVerticalCollisions(ArrayList<Platform> platforms) {
        Rectangle2D.Double myBounds = getBounds();
        for (Platform p : platforms) {
            Rectangle2D.Double pBounds = p.getBounds();
            if (myBounds.intersects(pBounds)) {
                if (vy > 0) {
                    // Landing on top: only if feet were above platform last frame
                    double playerBottom = y + HEIGHT;
                    double platTop = p.getY();
                    if (playerBottom - vy <= platTop + 5) {
                        y = p.getY() - HEIGHT;
                        vy = 0;
                        onGround = true;
                    }
                } else if (vy < 0 && p.isGround()) {
                    // Only block upward for ground segments
                    y = p.getY() + p.getHeight();
                    vy = 0;
                }
                myBounds = getBounds();
            }
        }
    }

    private void respawn() {
        x = 100;
        y = 800;
        vx = 0;
        vy = 0;
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        int drawX = (int) x - camX;
        int drawY = (int) y - camY;

        if (hasAtlasSprites) {
            drawSpriteSheet(g2, drawX, drawY);
        } else {
            drawFallback(g2, drawX, drawY);
        }

        // Draw star collect effect over player
        if (collectEffectTimer > 0 && starAnimation != null) {
            BufferedImage starFrame = starAnimation.getImage();
            if (starFrame != null) {
                int alpha = (int) (255 * (collectEffectTimer / (double) COLLECT_EFFECT_DURATION));
                java.awt.Composite oldComp = g2.getComposite();
                g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, alpha / 255.0f));
                g2.drawImage(starFrame,
                    drawX - 8, drawY - 8,
                    WIDTH + 16, HEIGHT + 16, null);
                g2.setComposite(oldComp);
            }
        }
    }

    private void drawSpriteSheet(Graphics2D g2, int drawX, int drawY) {
        // Pick the right frame based on state
        BufferedImage frame = null;
        if (Math.abs(vx) > 0 && runFrames != null && runFrames.length > 0) {
            // Running: cycle through run frames
            frame = runFrames[animFrame % runFrames.length];
        } else if (idleFrames != null && idleFrames.length > 0) {
            // Idle: cycle through idle frames
            frame = idleFrames[animFrame % idleFrames.length];
        }

        if (frame == null) { drawFallback(g2, drawX, drawY); return; }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(drawX + 4, drawY + HEIGHT - 6, WIDTH - 8, 10);

        // Scale the small pixel-art frame up to player size using nearest-neighbor
        java.awt.RenderingHints oldHints = g2.getRenderingHints();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        AffineTransform old = g2.getTransform();

        if (!facingRight) {
            g2.translate(drawX + WIDTH, drawY);
            g2.scale(-1, 1);
            g2.drawImage(frame, 0, 0, WIDTH, HEIGHT, null);
        } else {
            g2.drawImage(frame, drawX, drawY, WIDTH, HEIGHT, null);
        }

        g2.setTransform(old);
        g2.setRenderingHints(oldHints);
    }

    private void drawFallback(Graphics2D g2, int drawX, int drawY) {
        int legOffset = 0;
        if (Math.abs(vx) > 0 && onGround) {
            legOffset = (animFrame % 2 == 0) ? -3 : 3;
        }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(drawX + 4, drawY + HEIGHT - 6, WIDTH - 8, 10);

        // Legs
        g2.setColor(new Color(40, 60, 120));
        if (isJumping) {
            g2.fillRoundRect(drawX + 8, drawY + 34, 12, 14, 4, 4);
            g2.fillRoundRect(drawX + 28, drawY + 34, 12, 14, 4, 4);
        } else {
            g2.fillRoundRect(drawX + 10 + legOffset, drawY + 34, 10, 14, 4, 4);
            g2.fillRoundRect(drawX + 28 - legOffset, drawY + 34, 10, 14, 4, 4);
        }

        // Body
        GradientPaint bodyGrad = new GradientPaint(
            drawX, drawY + 16, new Color(60, 130, 220),
            drawX + WIDTH, drawY + 38, new Color(40, 100, 180));
        g2.setPaint(bodyGrad);
        g2.fillRoundRect(drawX + 8, drawY + 16, 32, 22, 6, 6);

        // Head
        Color skinColor = new Color(255, 220, 180);
        g2.setColor(skinColor);
        g2.fillOval(drawX + 10, drawY, 28, 20);

        // Hair
        g2.setColor(new Color(80, 50, 30));
        g2.fillArc(drawX + 9, drawY - 2, 30, 14, 0, 180);

        // Eye
        int eyeBaseX = facingRight ? drawX + 24 : drawX + 14;
        g2.setColor(Color.WHITE);
        g2.fillOval(eyeBaseX, drawY + 6, 8, 7);
        g2.setColor(new Color(40, 40, 40));
        g2.fillOval(eyeBaseX + 2, drawY + 7, 4, 5);

        // Arms
        g2.setColor(skinColor);
        int armSwing = (Math.abs(vx) > 0) ? legOffset : 0;
        g2.fillRoundRect(drawX + 2, drawY + 20 - armSwing, 8, 14, 4, 4);
        g2.fillRoundRect(drawX + 38, drawY + 20 + armSwing, 8, 14, 4, 4);
    }

    public void collectAnimal() {
        animalsCollected++;
        collectEffectTimer = COLLECT_EFFECT_DURATION;
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getAnimalsCollected() { return animalsCollected; }
    public boolean isOnGround() { return onGround; }
}
