package ImageManager;

import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageManager {

    public ImageManager() {}

    public static Image loadImage(String filename) {
        return new ImageIcon(filename).getImage();
    }

    public static BufferedImage loadBufferedImage(String filename) {
        BufferedImage bi = null;
        try {
            File file = new File(filename);
            bi = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("Error loading image: " + filename + " - " + e);
        }
        return bi;
    }

    public static BufferedImage copyImage(BufferedImage src) {
        if (src == null) return null;

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return copy;
    }
}