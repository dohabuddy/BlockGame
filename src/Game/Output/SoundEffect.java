package Game.Output;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SoundEffect {
    private final List<Clip> clipPool = new ArrayList<>();
    private final int poolSize;
    private int currentClipIndex = 0;

    public SoundEffect(String path, int poolSize) {
        this.poolSize = poolSize;
        preloadClips(path);
        preloadDummySound();
    }

    private void preloadClips(String path) {
        try {
            URL url = SoundEffect.class.getResource(path);
            if (url == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            AudioFormat format = audioInputStream.getFormat();
            byte[] audioData = audioInputStream.readAllBytes();

            for (int i = 0; i < poolSize; ++i) {
                Clip clip = AudioSystem.getClip();
                clip.open(new AudioInputStream(
                        new java.io.ByteArrayInputStream(audioData),
                        format,
                        audioData.length / format.getFrameSize()
                ));
                clipPool.add(clip);
            }

            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to load sound: " + e.getMessage());
        }
    }

    public void play(float volumeDb) {
        if (clipPool.isEmpty()) return;

        Clip clip = clipPool.get(currentClipIndex);
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);

        // Set volume
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(volumeDb);
        }

        clip.start();

        currentClipIndex = (currentClipIndex + 1) % poolSize;
    }
    public void stopAll() {
        if (clipPool.isEmpty()) return;

        for (Clip clip : clipPool) {
            if (clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0); // Optional: reset to start
            }
        }
    }
    private void preloadDummySound() {
        try {
            URL dummyURL = SoundPlayer.class.getResource("/Resources/SoundEffects/DUMMY.wav");
            if (dummyURL == null) {
                System.err.println("Dummy sound not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(dummyURL);
            Clip dummyClip = AudioSystem.getClip();
            dummyClip.open(audioIn);

            FloatControl gainControl = (FloatControl) dummyClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-80.0f); // Basically silent

            dummyClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Failed to preload dummy sound: " + e.getMessage());
        }
    }

}