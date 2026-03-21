package ImageManager;

import java.awt.image.BufferedImage;

public class GrayScaleFX implements ImageFX {

    @Override
    public BufferedImage apply(BufferedImage source) {
        if (source == null) return null;

        BufferedImage copy = ImageManager.copyImage(source);
        int w = copy.getWidth();
        int h = copy.getHeight();
        int[] pixels = new int[w * h];
        copy.getRGB(0, 0, w, h, pixels, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >> 24) & 255;
            int red = (pixels[i] >> 16) & 255;
            int green = (pixels[i] >> 8) & 255;
            int blue = pixels[i] & 255;

            // Weighted luminance formula (ITU-R BT.709)
            int gray = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

            pixels[i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
        }

        copy.setRGB(0, 0, w, h, pixels, 0, w);
        return copy;
    }
}
