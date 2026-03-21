package SoundManager;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;

public class SoundManager{
    HashMap<String, Clip> clips;
    private static SoundManager instance = null;
    private float volume;


    private SoundManager(){
        clips  = new HashMap<String, Clip>();

        Clip clip = loadClip("Ambience.wav");
        clips.put("background", clip);
        clip = loadClip("RainThunder.wav");
        clips.put("RainState", clip);

        volume = 1.0f;

    }

    public static SoundManager getInstance(){
        if (instance == null)
            instance = new SoundManager();
        return instance;
    }

    public Clip loadClip(String filename){
        AudioInputStream audioIn;
        Clip clip = null;
        try {
            File file = new File(filename);
            audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch(Exception e){
            System.out.println("Error File Not Found: " + e);
        }
        return clip;
    }

    public Clip getClip(String title){ return clips.get(title);}

    public void playClip(String title, boolean looping){
        Clip clip = getClip(title);
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gain.setValue(volume);
        if(clip != null){
            clip.setFramePosition(0);
            if (looping)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            else
                clip.start();
        }
    }

    public void setVolume(float newV){
        volume = newV;
    }

    public void stopClip(String title){
        Clip clip = getClip(title);
        if(clip != null){
            clip.stop();
        }
    }
}
