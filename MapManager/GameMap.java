package MapManager;

import Entities.Animal;
import Entities.Platform;
import Entities.PowerUp;
import Entities.Tree;
import ImageManager.AtlasLoader;
import ImageManager.ImageManager;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class GameMap {
    public static final int WORLD_WIDTH  = 6000;
    public static final int WORLD_HEIGHT = 1500;

    private ArrayList<Platform> platforms;
    private ArrayList<Tree> trees;
    private ArrayList<Animal> animals;
    private ArrayList<PowerUp> powerUps;

    public GameMap() {
        platforms = new ArrayList<>();
        trees     = new ArrayList<>();
        animals   = new ArrayList<>();
        powerUps  = new ArrayList<>();
        generateLevel();
    }

    private void generateLevel() {
        Random rand = new Random();

        // ===== Ground segments (with gaps for challenge) =====
        // Original zone  0 – 4000
        platforms.add(new Platform(   0, 900,  700, 50, true));
        platforms.add(new Platform( 800, 900,  500, 50, true));
        platforms.add(new Platform(1400, 900,  600, 50, true));
        platforms.add(new Platform(2100, 900,  700, 50, true));
        platforms.add(new Platform(2900, 900,  500, 50, true));
        platforms.add(new Platform(3500, 900,  600, 50, true));
        // Extended zone  4000 – 6000
        platforms.add(new Platform(4200, 900,  600, 50, true));
        platforms.add(new Platform(4900, 900,  500, 50, true));
        platforms.add(new Platform(5500, 900,  500, 50, true));

        int numGroundSegments = 9;

        // ===== Floating platforms – upper tier (visible, reachable by jumping) =====
        // --- Original zone ---
        platforms.add(new Platform( 150, 760, 180, 25, false));
        platforms.add(new Platform( 400, 660, 160, 25, false));
        platforms.add(new Platform( 650, 560, 200, 25, false));
        platforms.add(new Platform( 850, 720, 180, 25, false));
        platforms.add(new Platform(1050, 580, 220, 25, false));
        platforms.add(new Platform(1300, 460, 170, 25, false));
        platforms.add(new Platform(1500, 700, 190, 25, false));
        platforms.add(new Platform(1700, 530, 200, 25, false));
        platforms.add(new Platform(1950, 400, 180, 25, false));
        platforms.add(new Platform(2200, 680, 170, 25, false));
        platforms.add(new Platform(2400, 550, 220, 25, false));
        platforms.add(new Platform(2650, 430, 180, 25, false));
        platforms.add(new Platform(2900, 600, 200, 25, false));
        platforms.add(new Platform(3100, 480, 170, 25, false));
        platforms.add(new Platform(3350, 650, 190, 25, false));
        platforms.add(new Platform(3600, 550, 180, 25, false));
        platforms.add(new Platform(3800, 750, 150, 25, false));
        // --- Extended zone ---
        platforms.add(new Platform(4250, 760, 200, 25, false));
        platforms.add(new Platform(4480, 640, 170, 25, false));
        platforms.add(new Platform(4700, 520, 200, 25, false));
        platforms.add(new Platform(4950, 700, 190, 25, false));
        platforms.add(new Platform(5150, 570, 180, 25, false));
        platforms.add(new Platform(5350, 440, 210, 25, false));
        platforms.add(new Platform(5560, 680, 170, 25, false));
        platforms.add(new Platform(5750, 540, 200, 25, false));
        platforms.add(new Platform(5880, 400, 160, 25, false));

        // ===== Deep platforms (below ground, accessible through gaps) =====
        // Each gap has a small chain of ledges so the player can descend and return.
        // Gap 700-800
        platforms.add(new Platform( 680, 1040, 160, 25, false));
        platforms.add(new Platform( 720, 1200, 140, 25, false));
        platforms.add(new Platform( 660, 1360, 160, 25, false));
        // Gap 1300-1400
        platforms.add(new Platform(1280, 1060, 170, 25, false));
        platforms.add(new Platform(1310, 1230, 150, 25, false));
        platforms.add(new Platform(1270, 1390, 160, 25, false));
        // Gap 2000-2100
        platforms.add(new Platform(1990, 1050, 160, 25, false));
        platforms.add(new Platform(2020, 1220, 150, 25, false));
        platforms.add(new Platform(1980, 1380, 170, 25, false));
        // Gap 2800-2900
        platforms.add(new Platform(2790, 1070, 150, 25, false));
        platforms.add(new Platform(2820, 1240, 140, 25, false));
        // Gap 3500-3600 (adjusted – old last ground ends at 3500+500=4000, new gap before 4200)
        // Gap 4100-4200  (between old zone end and new ground at 4200)
        platforms.add(new Platform(4080, 1060, 160, 25, false));
        platforms.add(new Platform(4110, 1230, 150, 25, false));
        platforms.add(new Platform(4070, 1390, 160, 25, false));
        // Gap 4800-4900
        platforms.add(new Platform(4790, 1070, 150, 25, false));
        platforms.add(new Platform(4820, 1240, 140, 25, false));
        platforms.add(new Platform(4780, 1390, 160, 25, false));
        // Gap 5400-5500
        platforms.add(new Platform(5390, 1060, 160, 25, false));
        platforms.add(new Platform(5420, 1230, 150, 25, false));
        platforms.add(new Platform(5380, 1390, 170, 25, false));

        // ===== Trees (decorative, spread across full world width) =====
        String[] treeFiles = new String[17];
        for (int i = 0; i <= 16; i++) {
            treeFiles[i] = "Entities/Trees/sprite_" + String.format("%04d", i) + ".png";
        }

        int[] treeXPositions = {
             50,  250,  500,  850, 1050, 1450, 1650, 1900,
            2200, 2500, 2700, 3000, 3200, 3550, 3750,
            // Extended zone
            4250, 4500, 4750, 5000, 5200, 5550, 5750
        };
        for (int gx : treeXPositions) {
            if (rand.nextDouble() < 0.65) {
                String file = treeFiles[rand.nextInt(treeFiles.length)];
                BufferedImage img = ImageManager.loadBufferedImage(file);
                trees.add(new Tree(gx, 900, img));
            }
        }

        // ===== Animals (collectibles, placed on platforms) =====
        String[] animalNums = {"0001", "0002", "0003", "0004", "0007",
                               "0010", "0011", "0012", "0013",
                               "0018", "0019", "0020", "0023"};

        int numAnimals = 10 + rand.nextInt(11); // 10 to 20 animals
        int numFloating = platforms.size() - numGroundSegments;

        for (int i = 0; i < numAnimals; i++) {
            int platIdx;
            // 70% chance on floating platforms, 30% on ground
            if (rand.nextDouble() < 0.7 && numFloating > 0) {
                platIdx = numGroundSegments + rand.nextInt(numFloating);
            } else {
                platIdx = rand.nextInt(numGroundSegments);
            }
            Platform plat = platforms.get(platIdx);

            int ax = plat.getX() + rand.nextInt(Math.max(1, plat.getWidth() - 40));
            int ay = plat.getY() - 45;

            String animalFile = "Entities/Animals/sprite_"
                + animalNums[rand.nextInt(animalNums.length)] + ".png";
            BufferedImage img = ImageManager.loadBufferedImage(animalFile);
            animals.add(new Animal(ax, ay, img));
        }

        // ===== Power-ups (StarActor sprites from atlas) =====
        AtlasLoader atlas = new AtlasLoader("Entities/PlayerSprite.atlas");
        ArrayList<BufferedImage> starFrames = atlas.getFrameImages("StarActor");

        int numPowerUps = 3 + rand.nextInt(4); // 3-6 power-ups across larger map
        for (int i = 0; i < numPowerUps; i++) {
            PowerUp.Type type = rand.nextBoolean()
                ? PowerUp.Type.SPEED_BOOST
                : PowerUp.Type.TIMER_FREEZE;
            int platIdx = numGroundSegments + rand.nextInt(numFloating);
            Platform plat = platforms.get(platIdx);
            int px = plat.getX() + rand.nextInt(Math.max(1, plat.getWidth() - 40));
            int py = plat.getY() - 52;
            powerUps.add(new PowerUp(px, py, type, starFrames));
        }
    }

    public ArrayList<Platform> getPlatforms() { return platforms; }
    public ArrayList<Tree>     getTrees()     { return trees; }
    public ArrayList<Animal>   getAnimals()   { return animals; }
    public ArrayList<PowerUp>  getPowerUps()  { return powerUps; }
    public int getTotalAnimals() { return animals.size(); }

    public int getCollectedCount() {
        int count = 0;
        for (Animal a : animals) {
            if (a.isCollected()) count++;
        }
        return count;
    }
}
