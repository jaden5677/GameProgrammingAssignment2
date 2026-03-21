package SoundManager;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;

public class SoundManager {
    HashMap<String, Clip> clips;
    private static SoundManager instance = null;
    private float volume;

    private SoundManager() {
        clips = new HashMap<String, Clip>();

        Clip clip = loadClip("SoundManager/Ambience.wav");
        clips.put("background", clip);
        clip = loadClip("SoundManager/RainThunder.wav");
        clips.put("RainState", clip);
        clip = loadClip("SoundManager/jump.wav");
        clips.put("jump", clip);
        clip = loadClip("SoundManager/collect.wav");
        clips.put("collect", clip);
        clip = loadClip("SoundManager/win.wav");
        clips.put("win", clip);

        volume = 1.0f;
    }

    public static SoundManager getInstance() {
        if (instance == null)
            instance = new SoundManager();
        return instance;
    }

    public Clip loadClip(String filename) {
        AudioInputStream audioIn;
        Clip clip = null;
        try {
            File file = new File(filename);
            audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Exception e) {
            System.out.println("Sound not found (non-critical): " + filename);
        }
        return clip;
    }

    public Clip getClip(String title) {
        return clips.get(title);
    }

    public void playClip(String title, boolean looping) {
        Clip clip = getClip(title);
        if (clip != null) {
            // Apply volume using MASTER_GAIN control
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(Math.max(volume, 0.0001)));
                dB = Math.max(dB, gain.getMinimum());
                dB = Math.min(dB, gain.getMaximum());
                gain.setValue(dB);
            } catch (Exception e) {
                // Some clips may not support MASTER_GAIN
            }
            clip.setFramePosition(0);
            if (looping)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            else
                clip.start();
        }
    }

    public void setVolume(float newV) {
        volume = Math.max(0.0f, Math.min(1.0f, newV));
    }

    public float getVolume() {
        return volume;
    }

    public void stopClip(String title) {
        Clip clip = getClip(title);
        if (clip != null) {
            clip.stop();
        }
    }
}
