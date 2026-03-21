package MainClass;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import Entities.Animal;
import Entities.Platform;
import Entities.Player;
import Entities.PowerUp;
import Entities.Tree;
import MapManager.Camera;
import MapManager.GameMap;
import SoundManager.SoundManager;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static final int PWIDTH = 800;
    public static final int PHEIGHT = 600;

    private Thread gameThread;
    private boolean isRunning;
    private boolean isPaused;
    private boolean gameStarted;
    private boolean gameWon;
    private boolean gameLost;

    private Player player;
    private GameMap gameMap;
    private Camera camera;
    private SoundManager soundManager;

    private BufferedImage dbImage;   // double buffer

    // Key states
    private boolean keyLeft, keyRight, keyUp, keyJump;

    // HUD/game-state timing
    private static final int GAME_DURATION_MS = 60_000;
    private int remainingTimeMs;

    // FPS tracker
    private int currentFps;


    public GamePanel() {
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        isRunning = false;
        isPaused = false;
        gameStarted = false;
        gameWon = false;
        gameLost = false;
        remainingTimeMs = GAME_DURATION_MS;
        currentFps = 0;

        soundManager = SoundManager.getInstance();
        dbImage = new BufferedImage(PWIDTH, PHEIGHT, BufferedImage.TYPE_INT_ARGB);
        //atmosphereTint = new TintFX(0, 0, 0);

        // Render initial start screen
        gameRender();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dbImage != null) {
            g.drawImage(dbImage, 0, 0, null);
        }
    }

    public void createGameEntities() {
        gameMap = new GameMap();
        player = new Player(100, 800);
        camera = new Camera(PWIDTH, PHEIGHT, GameMap.WORLD_WIDTH, GameMap.WORLD_HEIGHT);
    }

    public void startGame() {
        if (!gameStarted) {
            createGameEntities();
            gameStarted = true;
            gameWon = false;
            gameLost = false;
            isPaused = false;
            isRunning = true;
            remainingTimeMs = GAME_DURATION_MS;
            currentFps = 0;
            keyLeft = keyRight = keyUp = keyJump = false;

            gameThread = new Thread(this);
            gameThread.start();

            soundManager.playClip("background", true);
        }
    }

    public void pauseGame() {
        if (gameStarted) {
            isPaused = !isPaused;
        }
    }

    public void endGame() {
        isRunning = false;
        soundManager.stopClip("background");
        gameStarted = false;
    }

    public boolean isGameStarted() { return gameStarted; }
    public boolean isGamePaused() { return isPaused; }

    // ==================== Game Loop ====================

    @Override
    public void run() {
        long lastTick = System.currentTimeMillis();
        long fpsWindowStart = lastTick;
        int frameCounter = 0;

        try {
            while (isRunning) {
                long now = System.currentTimeMillis();
                int elapsedMs = (int) (now - lastTick);
                if (elapsedMs < 0) elapsedMs = 0;
                lastTick = now;

                if (!isPaused) {
                    gameUpdate(elapsedMs);
                }
                gameRender();
                paintScreen();

                frameCounter++;
                if (now - fpsWindowStart >= 1000) {
                    currentFps = frameCounter;
                    frameCounter = 0;
                    fpsWindowStart = now;
                }

                Thread.sleep(20); // ~50 FPS
            }
        } catch (InterruptedException e) {
            System.out.println("Game thread interrupted");
        }
    }

    private void gameUpdate(int elapsedMs) {
        if (player == null || gameWon || gameLost) return;

        if (!player.isTimerFrozen()) {
            remainingTimeMs -= elapsedMs;
        }
        if (remainingTimeMs <= 0) {
            remainingTimeMs = 0;
            gameLost = true;
            isRunning = false;
            soundManager.stopClip("background");
            return;
        }

        player.update(keyLeft, keyRight, keyJump || keyUp, gameMap.getPlatforms());

        camera.follow(
            player.getX() + Player.WIDTH / 2.0,
            player.getY() + Player.HEIGHT / 2.0
        );

        for (Animal animal : gameMap.getAnimals()) {
            animal.update();

            if (!animal.isCollected() && player.getBounds().intersects(animal.getBounds())) {
                animal.collect();
                player.collectAnimal();
                soundManager.playClip("collect", false);
            }
        }

        // Power-up pickup
        for (PowerUp pu : gameMap.getPowerUps()) {
            pu.update();
            if (!pu.isCollected() && player.getBounds().intersects(pu.getBounds())) {
                pu.collect();
                Player.PowerUpType effect = (pu.getType() == PowerUp.Type.SPEED_BOOST)
                    ? Player.PowerUpType.SPEED_BOOST
                    : Player.PowerUpType.TIMER_FREEZE;
                player.applyPowerUp(effect);
                soundManager.playClip("collect", false);
            }
        }

        if (gameMap.getCollectedCount() == gameMap.getTotalAnimals()) {
            gameWon = true;
            isRunning = false;
            soundManager.stopClip("background");
            soundManager.playClip("win", false);
        }
    }

    // ==================== Rendering ====================

    private void gameRender() {
        Graphics2D g2 = (Graphics2D) dbImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameStarted) {
            drawStartScreen(g2);
            g2.dispose();
            return;
        }

        int camX = camera.getX();
        int camY = camera.getY();

        drawBackground(g2, camX, camY);

        // Trees behind other entities
        if (gameMap != null) {
            for (Tree tree : gameMap.getTrees()) {
                tree.draw(g2, camX, camY);
            }
            for (Platform platform : gameMap.getPlatforms()) {
                platform.draw(g2, camX, camY);
            }
            for (Animal animal : gameMap.getAnimals()) {
                animal.draw(g2, camX, camY);
            }
            for (PowerUp pu : gameMap.getPowerUps()) {
                pu.draw(g2, camX, camY);
            }
        }

        if (player != null) {
            player.draw(g2, camX, camY);
        }

        drawHUD(g2);

        if (isPaused) drawPauseOverlay(g2);
        if (gameWon) drawWinScreen(g2);
        if (gameLost) drawLoseScreen(g2);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2, int camX, int camY) {
        // Sky gradient
        GradientPaint sky = new GradientPaint(
            0, 0, new Color(25, 100, 180),
            0, PHEIGHT, new Color(135, 206, 235));
        g2.setPaint(sky);
        g2.fillRect(0, 0, PWIDTH, PHEIGHT);

        // Sun
        int sunX = PWIDTH - 120 - (int)(camX * 0.05);
        g2.setColor(new Color(255, 240, 100, 60));
        g2.fillOval(sunX - 20, 20, 100, 100);
        g2.setColor(new Color(255, 230, 80));
        g2.fillOval(sunX, 30, 60, 60);

        // Clouds (very slow parallax)
        drawClouds(g2, camX);

        // Distant mountains (parallax at 20% speed)
        g2.setColor(new Color(100, 130, 160, 180));
        int mOff = (int)(camX * 0.2);
        int[] mx1 = {0 - mOff, 200 - mOff, 350 - mOff, 500 - mOff, 700 - mOff,
                      900 - mOff, 1100 - mOff, 1300 - mOff, PWIDTH + 200 - mOff};
        int[] my1 = {PHEIGHT, 300, 350, 280, 320, 260, 310, 290, PHEIGHT};
        g2.fillPolygon(mx1, my1, mx1.length);

        // Near mountains (parallax at 40% speed)
        g2.setColor(new Color(70, 100, 70, 160));
        int mOff2 = (int)(camX * 0.4);
        int[] mx2 = {0 - mOff2, 250 - mOff2, 450 - mOff2, 650 - mOff2,
                      850 - mOff2, 1050 - mOff2, PWIDTH + 200 - mOff2};
        int[] my2 = {PHEIGHT, 380, 340, 400, 350, 380, PHEIGHT};
        g2.fillPolygon(mx2, my2, mx2.length);
    }

    private void drawClouds(Graphics2D g2, int camX) {
        g2.setColor(new Color(255, 255, 255, 90));
        int cOff = (int)(camX * 0.08);
        int[][] clouds = {
            {100, 60, 130, 45}, {160, 48, 80, 35},
            {400, 85, 150, 50}, {470, 68, 100, 40},
            {750, 40, 140, 50}, {820, 55, 80, 35},
            {1050, 90, 120, 40}, {1350, 50, 150, 50}
        };
        for (int[] c : clouds) {
            g2.fillOval(c[0] - cOff, c[1], c[2], c[3]);
        }
    }

    private void drawHUD(Graphics2D g2) {
        if (player == null || gameMap == null) return;

        // Score background
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(10, 10, 260, 45, 12, 12);

        // Animal icon (simple)
        g2.setColor(new Color(255, 200, 50));
        g2.fillOval(20, 18, 28, 28);
        g2.setColor(new Color(200, 150, 30));
        g2.drawOval(20, 18, 28, 28);

        // Score text
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(255, 255, 255));
        String scoreText = gameMap.getCollectedCount() + " / " + gameMap.getTotalAnimals() + " Animals";
        g2.drawString(scoreText, 55, 39);

        // Timer (top-right)
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(PWIDTH - 170, 10, 155, 45, 12, 12);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(255, 255, 255));
        g2.drawString("Time: " + Math.max(0, remainingTimeMs / 1000), PWIDTH - 155, 39);

        // FPS counter
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(10, 60, 100, 28, 10, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(220, 255, 220));
        g2.drawString("FPS: " + currentFps, 22, 79);

        // Active effect indicator (directly below FPS)
        String effectLabel = player.getActivePowerUpLabel();
        boolean hasEffect  = effectLabel != null;
        g2.setColor(hasEffect ? new Color(0, 0, 0, 160) : new Color(0, 0, 0, 80));
        g2.fillRoundRect(10, 93, 185, 28, 10, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        if (hasEffect) {
            g2.setColor(player.getActivePowerUp() == Player.PowerUpType.SPEED_BOOST
                ? new Color(100, 220, 255) : new Color(255, 220, 60));
            g2.drawString(effectLabel, 18, 112);
        } else {
            g2.setColor(new Color(180, 180, 180, 130));
            g2.drawString("No Active Effect", 18, 112);
        }

        // Controls hint
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString("WASD/Arrows: Move | SPACE: Jump | P: Pause | ESC: End", PWIDTH - 370, 20);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, PWIDTH, PHEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        String text = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (PWIDTH - fm.stringWidth(text)) / 2, PHEIGHT / 2);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String sub = "Press P or click Pause to resume";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (PWIDTH - fm.stringWidth(sub)) / 2, PHEIGHT / 2 + 40);
    }

    private void drawWinScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, PWIDTH, PHEIGHT);

        g2.setColor(new Color(255, 220, 50));
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        String text = "YOU WIN!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (PWIDTH - fm.stringWidth(text)) / 2, PHEIGHT / 2 - 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        String sub = "All " + gameMap.getTotalAnimals() + " animals collected in time!";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (PWIDTH - fm.stringWidth(sub)) / 2, PHEIGHT / 2 + 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String restart = "Press End Game then Start Game to play again";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(restart, (PWIDTH - fm.stringWidth(restart)) / 2, PHEIGHT / 2 + 70);
    }

    private void drawLoseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, PWIDTH, PHEIGHT);

        g2.setColor(new Color(255, 120, 120));
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        String text = "TIME UP!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (PWIDTH - fm.stringWidth(text)) / 2, PHEIGHT / 2 - 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        String sub = "You failed to collect all animals in 30 seconds.";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (PWIDTH - fm.stringWidth(sub)) / 2, PHEIGHT / 2 + 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String restart = "Press Start Game to try again";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(210, 210, 210));
        g2.drawString(restart, (PWIDTH - fm.stringWidth(restart)) / 2, PHEIGHT / 2 + 70);
    }

    private void drawStartScreen(Graphics2D g2) {
        GradientPaint sky = new GradientPaint(
            0, 0, new Color(20, 60, 120),
            0, PHEIGHT, new Color(50, 120, 160));
        g2.setPaint(sky);
        g2.fillRect(0, 0, PWIDTH, PHEIGHT);

        // Decorative mountains
        g2.setColor(new Color(40, 80, 60, 180));
        int[] mx = {0, 150, 300, 450, 600, 800};
        int[] my = {PHEIGHT, 350, 400, 320, 380, PHEIGHT};
        g2.fillPolygon(mx, my, mx.length);

        // Title
        g2.setColor(new Color(255, 220, 50));
        g2.setFont(new Font("Arial", Font.BOLD, 56));
        String title = "ANIMAL RESCUE";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (PWIDTH - fm.stringWidth(title)) / 2, PHEIGHT / 2 - 80);

        // Subtitle
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        String sub = "Explore the world and collect all the animals!";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (PWIDTH - fm.stringWidth(sub)) / 2, PHEIGHT / 2 - 30);

        // Instructions
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(200, 220, 255));
        String[] lines = {
            "Arrow Keys / WASD - Move & Jump",
            "SPACE - Jump",
            "P - Pause     ESC - End Game"
        };
        for (int i = 0; i < lines.length; i++) {
            fm = g2.getFontMetrics();
            g2.drawString(lines[i], (PWIDTH - fm.stringWidth(lines[i])) / 2, PHEIGHT / 2 + 20 + i * 28);
        }

        // Start prompt
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        String start = "Click  Start Game  to begin";
        fm = g2.getFontMetrics();
        g2.drawString(start, (PWIDTH - fm.stringWidth(start)) / 2, PHEIGHT / 2 + 130);
    }

    private void paintScreen() {
        try {
            Graphics g = this.getGraphics();
            if (g != null && dbImage != null) {
                g.drawImage(dbImage, 0, 0, null);
                g.dispose();
            }
        } catch (Exception e) {
            System.out.println("Graphics error: " + e);
        }
    }

    // ==================== Input ====================

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: case KeyEvent.VK_A:
                keyLeft = true; break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                keyRight = true; break;
            case KeyEvent.VK_UP: case KeyEvent.VK_W:
                keyUp = true; break;
            case KeyEvent.VK_SPACE:
                keyJump = true; break;
            case KeyEvent.VK_P:
                pauseGame(); break;
            case KeyEvent.VK_ESCAPE:
                endGame(); break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: case KeyEvent.VK_A:
                keyLeft = false; break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                keyRight = false; break;
            case KeyEvent.VK_UP: case KeyEvent.VK_W:
                keyUp = false; break;
            case KeyEvent.VK_SPACE:
                keyJump = false; break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
