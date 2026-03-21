package ImageManager;

import java.awt.image.BufferedImage;

public class DissapearFX implements ImageFX {
    private int alpha;

    public DissapearFX() {
        this.alpha = 255;
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
    }

    public int getAlpha() {
        return alpha;
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
            int origAlpha = (pixels[i] >> 24) & 255;
            if (origAlpha == 0) continue;

            int red = (pixels[i] >> 16) & 255;
            int green = (pixels[i] >> 8) & 255;
            int blue = pixels[i] & 255;

            int newAlpha = (int) (origAlpha * (alpha / 255.0));
            pixels[i] = (newAlpha << 24) | (red << 16) | (green << 8) | blue;
        }

        copy.setRGB(0, 0, w, h, pixels, 0, w);
        return copy;
    }
}
