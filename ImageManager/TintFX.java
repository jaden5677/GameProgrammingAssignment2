package ImageManager;

import java.awt.image.BufferedImage;

public class TintFX implements ImageFX {
    private int tintRed, tintGreen, tintBlue;

    public TintFX(int r, int g, int b) {
        this.tintRed = r;
        this.tintGreen = g;
        this.tintBlue = b;
    }

    public void setTint(int r, int g, int b) {
        this.tintRed = r;
        this.tintGreen = g;
        this.tintBlue = b;
    }

    private int truncate(int value) {
        return Math.max(0, Math.min(255, value));
    }

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

            red = truncate(red + tintRed);
            green = truncate(green + tintGreen);
            blue = truncate(blue + tintBlue);

            pixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        copy.setRGB(0, 0, w, h, pixels, 0, w);
        return copy;
    }
}
