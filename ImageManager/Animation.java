package ImageManager;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Animation {
    private ArrayList<BufferedImage> frames;
    private ArrayList<Long> durations;
    private int currentFrame;
    private long animTime;
    private long totalDuration;

    public Animation() {
        frames = new ArrayList<>();
        durations = new ArrayList<>();
        currentFrame = 0;
        animTime = 0;
        totalDuration = 0;
    }

    public void addFrame(BufferedImage image, long duration) {
        frames.add(image);
        totalDuration += duration;
        durations.add(totalDuration);
    }

    public void update(long elapsed) {
        if (frames.size() <= 1) return;

        animTime += elapsed;
        if (animTime >= totalDuration) {
            animTime = animTime % totalDuration;
        }

        for (int i = 0; i < durations.size(); i++) {
            if (animTime < durations.get(i)) {
                currentFrame = i;
                break;
            }
        }
    }

    public BufferedImage getImage() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrame);
    }

    public int getNumFrames() {
        return frames.size();
    }

    public void reset() {
        currentFrame = 0;
        animTime = 0;
    }
}
