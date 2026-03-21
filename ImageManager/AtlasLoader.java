package ImageManager;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses a LibGDX-style texture atlas file and extracts sub-images
 * (sprite regions) from the associated sprite sheet PNG.
 */
public class AtlasLoader {

    private BufferedImage sheetImage;
    private HashMap<String, ArrayList<AtlasRegion>> regions;

    public static class AtlasRegion {
        public String name;
        public int x, y, width, height;
        public int index;
        public boolean rotate;
        public BufferedImage image;

        @Override
        public String toString() {
            return name + "[" + index + "] (" + x + "," + y + " " + width + "x" + height + ")";
        }
    }

    public AtlasLoader(String atlasPath) {
        regions = new HashMap<>();
        parseAtlas(atlasPath);
    }

    private void parseAtlas(String atlasPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(atlasPath))) {
            String line;

            // First non-empty line: image filename
            String imageFile = null;
            while ((imageFile = reader.readLine()) != null) {
                imageFile = imageFile.trim();
                if (!imageFile.isEmpty()) break;
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                // Resolve relative to atlas directory
                File atlasFile = new File(atlasPath);
                File parentDir = atlasFile.getParentFile();
                File imgFile = (parentDir != null)
                    ? new File(parentDir, imageFile)
                    : new File(imageFile);

                sheetImage = ImageManager.loadBufferedImage(imgFile.getPath());
                if (sheetImage == null) {
                    System.out.println("AtlasLoader: Sheet image not found at " + imgFile.getPath());
                    System.out.println("AtlasLoader: Will use fallback rendering for sprites.");
                } else {
                    System.out.println("AtlasLoader: Loaded sheet " + imgFile.getPath()
                        + " (" + sheetImage.getWidth() + "x" + sheetImage.getHeight()
                        + " type=" + sheetImage.getType() + ")");
                }
            }

            // Skip header lines (size, format, filter, repeat)
            String currentRegionName = null;
            AtlasRegion currentRegion = null;
            boolean inRegions = false; // true once we've seen the first region name

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Header lines (before any regions) start with known keywords
                if (!inRegions && (line.startsWith("size:") || line.startsWith("format:")
                    || line.startsWith("filter:") || line.startsWith("repeat:"))) {
                    continue;
                }

                // Region properties are indented (contain ':')
                // Region names do NOT contain ':'
                if (!line.contains(":")) {
                    // This is a new region name
                    inRegions = true;
                    currentRegionName = line;
                    currentRegion = new AtlasRegion();
                    currentRegion.name = currentRegionName;
                    continue;
                }

                // Parse region properties
                if (currentRegion == null) continue;

                if (line.startsWith("rotate:")) {
                    currentRegion.rotate = line.contains("true");
                } else if (line.startsWith("xy:")) {
                    String[] parts = line.substring(3).trim().split(",\\s*");
                    currentRegion.x = Integer.parseInt(parts[0].trim());
                    currentRegion.y = Integer.parseInt(parts[1].trim());
                } else if (line.startsWith("size:")) {
                    String[] parts = line.substring(5).trim().split(",\\s*");
                    currentRegion.width = Integer.parseInt(parts[0].trim());
                    currentRegion.height = Integer.parseInt(parts[1].trim());
                } else if (line.startsWith("index:")) {
                    currentRegion.index = Integer.parseInt(line.substring(6).trim());

                    // Extract the sub-image from the sheet
                    if (sheetImage != null
                        && currentRegion.width > 0 && currentRegion.height > 0
                        && currentRegion.x + currentRegion.width <= sheetImage.getWidth()
                        && currentRegion.y + currentRegion.height <= sheetImage.getHeight()) {
                        System.out.println("AtlasLoader: Extracting " + currentRegion);
                        currentRegion.image = sheetImage.getSubimage(
                            currentRegion.x, currentRegion.y,
                            currentRegion.width, currentRegion.height);
                    } else if (sheetImage != null) {
                        System.out.println("AtlasLoader: Skipping region " + currentRegion
                            + " sheet=" + sheetImage.getWidth() + "x" + sheetImage.getHeight());
                    }

                    // Store in map
                    regions.computeIfAbsent(currentRegion.name, k -> new ArrayList<>());
                    regions.get(currentRegion.name).add(currentRegion);

                    // Prepare for possible next region with same name
                    String savedName = currentRegion.name;
                    currentRegion = new AtlasRegion();
                    currentRegion.name = savedName;
                }
            }
        } catch (IOException e) {
            System.out.println("AtlasLoader: Error reading atlas: " + e);
        }
    }

    /**
     * Get all regions matching a sprite name, sorted by index.
     */
    public ArrayList<AtlasRegion> getRegions(String name) {
        ArrayList<AtlasRegion> list = regions.get(name);
        if (list == null) return new ArrayList<>();
        list.sort((a, b) -> Integer.compare(a.index, b.index));
        return list;
    }

    /**
     * Get the first (or only) region for a sprite name.
     */
    public AtlasRegion getRegion(String name) {
        ArrayList<AtlasRegion> list = getRegions(name);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Get all frame images for an animated sprite, in index order.
     */
    public ArrayList<BufferedImage> getFrameImages(String name) {
        ArrayList<AtlasRegion> list = getRegions(name);
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (AtlasRegion r : list) {
            if (r.image != null) {
                images.add(r.image);
            }
        }
        return images;
    }

    public boolean hasSheet() {
        return sheetImage != null;
    }

    public BufferedImage getSheetImage() {
        return sheetImage;
    }
}
