import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class AnalyzeKnight {
    public static void main(String[] args) throws Exception {
        BufferedImage sheet = ImageIO.read(new File("Entities/game.png"));
        BufferedImage knight = sheet.getSubimage(260, 20, 256, 256);

        // Find row bands
        int[][] rowBands = findRowBands(knight);
        System.out.println("Found " + rowBands.length + " animation rows:");

        String[] labels = {"Row1(IDLE?)", "Row2", "Row3(RUN?)", "Row4(RUN2?)",
                           "Row5(ROLL?)", "Row6", "Row7(HIT?)", "Row8(DEATH?)"};
        for (int r = 0; r < rowBands.length; r++) {
            int yStart = rowBands[r][0];
            int yEnd = rowBands[r][1];
            String label = r < labels.length ? labels[r] : "Row" + (r+1);
            System.out.println("\n" + label + ": y=" + yStart + ".." + yEnd + " (height=" + (yEnd-yStart+1) + ")");

            // Find frame columns within this row band
            ArrayList<int[]> frames = findFramesInRow(knight, yStart, yEnd);
            for (int f = 0; f < frames.size(); f++) {
                int[] frame = frames.get(f);
                System.out.println("  Frame " + f + ": x=" + frame[0] + ".." + frame[1]
                    + " y=" + frame[2] + ".." + frame[3]
                    + " (" + (frame[1]-frame[0]+1) + "x" + (frame[3]-frame[2]+1) + ")");
            }
        }
    }

    static int[][] findRowBands(BufferedImage img) {
        ArrayList<int[]> bands = new ArrayList<>();
        boolean inContent = false;
        int startY = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            boolean hasContent = false;
            for (int x = 0; x < img.getWidth(); x++) {
                if (((img.getRGB(x, y) >> 24) & 0xFF) > 10) { hasContent = true; break; }
            }
            if (hasContent && !inContent) { startY = y; inContent = true; }
            else if (!hasContent && inContent) { bands.add(new int[]{startY, y - 1}); inContent = false; }
        }
        if (inContent) bands.add(new int[]{startY, img.getHeight() - 1});
        return bands.toArray(new int[0][]);
    }

    static ArrayList<int[]> findFramesInRow(BufferedImage img, int yStart, int yEnd) {
        ArrayList<int[]> frames = new ArrayList<>();
        boolean inFrame = false;
        int frameStartX = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            boolean hasContent = false;
            for (int y = yStart; y <= yEnd; y++) {
                if (((img.getRGB(x, y) >> 24) & 0xFF) > 10) { hasContent = true; break; }
            }
            if (hasContent && !inFrame) { frameStartX = x; inFrame = true; }
            else if (!hasContent && inFrame) {
                // Found a frame - get tight Y bounds
                int minY = yEnd, maxY = yStart;
                for (int fy = yStart; fy <= yEnd; fy++) {
                    for (int fx = frameStartX; fx < x; fx++) {
                        if (((img.getRGB(fx, fy) >> 24) & 0xFF) > 10) {
                            minY = Math.min(minY, fy);
                            maxY = Math.max(maxY, fy);
                            break;
                        }
                    }
                }
                frames.add(new int[]{frameStartX, x - 1, minY, maxY});
                inFrame = false;
            }
        }
        if (inFrame) {
            int minY = yEnd, maxY = yStart;
            for (int fy = yStart; fy <= yEnd; fy++) {
                for (int fx = frameStartX; fx < img.getWidth(); fx++) {
                    if (((img.getRGB(fx, fy) >> 24) & 0xFF) > 10) {
                        minY = Math.min(minY, fy);
                        maxY = Math.max(maxY, fy);
                        break;
                    }
                }
            }
            frames.add(new int[]{frameStartX, img.getWidth() - 1, minY, maxY});
        }
        return frames;
    }
}
