package Game.Output;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicPlayer {
    private final Map<MusicTracks, Clip[]> clipMap = new HashMap<>();
    private final Map<MusicTracks, Float> beatDurationMap = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Clip activeClip = null;
    private MusicTracks currentTrack = null;
    public final float MASTER_VOLUME = -5.0f;
    public float masterVolumeDb;
    public float unmutedVolume;
    public boolean muted;
    private int clipIndex = 0;

    public MusicPlayer() {
        masterVolumeDb = MASTER_VOLUME;
        preload(MusicTracks.MAIN_MENU, "/Resources/Music/MUSIC_MAIN_MENU.wav");
        preload(MusicTracks.LEVEL_1, "/Resources/Music/MUSIC_LEVEL_1.wav");
        preload(MusicTracks.LEVEL_5, "/Resources/Music/MUSIC_LEVEL_5.wav");
        preload(MusicTracks.LEVEL_10, "/Resources/Music/MUSIC_LEVEL_10.wav");
        preload(MusicTracks.LEVEL_15, "/Resources/Music/MUSIC_LEVEL_15.wav");
        preload(MusicTracks.LEVEL_20, "/Resources/Music/MUSIC_LEVEL_20.wav");
        preload(MusicTracks.LEVEL_25, "/Resources/Music/MUSIC_LEVEL_25.wav");
        preload(MusicTracks.LEVEL_30, "/Resources/Music/MUSIC_LEVEL_30.wav");
        preload(MusicTracks.LEVEL_35, "/Resources/Music/MUSIC_LEVEL_35.wav");
        preload(MusicTracks.LEVEL_40, "/Resources/Music/MUSIC_LEVEL_40.wav");
        preload(MusicTracks.GAME_OVER, "/Resources/Music/MUSIC_GAME_OVER.wav");


        beatDurationMap.put(MusicTracks.MAIN_MENU, 666.7f);
        beatDurationMap.put(MusicTracks.LEVEL_1, 600.0f);
        beatDurationMap.put(MusicTracks.LEVEL_5, 545.5f);
        beatDurationMap.put(MusicTracks.LEVEL_10, 500.0f);
        beatDurationMap.put(MusicTracks.LEVEL_15, 461.5f);
        beatDurationMap.put(MusicTracks.LEVEL_20, 428.6f);
        beatDurationMap.put(MusicTracks.LEVEL_25, 400.0f);
        beatDurationMap.put(MusicTracks.LEVEL_30, 375.0f);
        beatDurationMap.put(MusicTracks.LEVEL_35, 352.94f);
        beatDurationMap.put(MusicTracks.LEVEL_40, 333.3f);
        beatDurationMap.put(MusicTracks.GAME_OVER, 600.0f);

    }

    private void preload(MusicTracks track, String path) {
        try {
            URL url = MusicPlayer.class.getResource(path);
            if (url == null) throw new IOException("Music file not found: " + path);

            AudioInputStream audioIn1 = AudioSystem.getAudioInputStream(url);
            Clip clip1 = AudioSystem.getClip();
            clip1.open(audioIn1);

            AudioInputStream audioIn2 = AudioSystem.getAudioInputStream(url);
            Clip clip2 = AudioSystem.getClip();
            clip2.open(audioIn2);

            clipMap.put(track, new Clip[] { clip1, clip2 });
        } catch (Exception e) {
            System.err.println("Failed to preload " + track + ": " + e.getMessage());
        }
    }

    public void startWithDelay(MusicTracks newTrack) {
        int delayMs = 50;
        final MusicTracks previousTrack = currentTrack;
        final long previousMicros = (activeClip != null) ? activeClip.getMicrosecondPosition() : 0;

        if (activeClip != null && activeClip.isRunning()) {
            activeClip.stop();
        }

        scheduler.schedule(() -> startMusicAtMicros(newTrack, previousTrack, previousMicros), delayMs, TimeUnit.MILLISECONDS);
    }

    private void startMusicAtMicros(MusicTracks newTrack, MusicTracks fromTrack, long fromMicros) {
        Clip[] trackClips = clipMap.get(newTrack);
        if (trackClips == null) return;

        if (activeClip != null && activeClip.isRunning()) {
            activeClip.stop();
        }

        clipIndex = (clipIndex + 1) % 2;
        activeClip = trackClips[clipIndex];

        if (fromMicros > 0 && beatDurationMap.containsKey(fromTrack) && beatDurationMap.containsKey(newTrack)) {
            float fromBeatDurationMs = beatDurationMap.get(fromTrack);
            float toBeatDurationMs = beatDurationMap.get(newTrack);
            // fromMicros is in microseconds; convert to milliseconds first
            double fromMillis = fromMicros / 1000.0;

            // Convert time into beats
            double beatCount = fromMillis / fromBeatDurationMs;

            // Convert beat count back to time in new tempo (in ms)
            double adjustedMillis = beatCount * toBeatDurationMs;

            // Convert back to microseconds
            long adjustedMicros = (long)(adjustedMillis * 1000);

            try {
                activeClip.setMicrosecondPosition(adjustedMicros);
            } catch (Exception ignored) {
                activeClip.setMicrosecondPosition(0);
            }
        } else {
            activeClip.setMicrosecondPosition(0);
        }

        if (newTrack == MusicTracks.MAIN_MENU) {
            setGain(activeClip, masterVolumeDb);
            activeClip.loop(Clip.LOOP_CONTINUOUSLY);
            activeClip.start();
        } else {
            fadeIn(activeClip);
        }

        currentTrack = newTrack;
    }
    private void fadeIn(Clip clip) {
        final int steps = 40;
        final long delayMs = 50;
        float startGain = -80f;
        float endGain = masterVolumeDb;

        setGain(clip, startGain);
        clip.setFramePosition(clip.getFramePosition());
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

        for (int i = 0; i <= steps; i++) {
            final int step = i;
            scheduler.schedule(() -> {
                float progress = (float) step / steps;
                float eased = (float) Math.log10(9 * progress + 1);
                float gain = startGain + (endGain - startGain) * eased;
                setGain(clip, gain);
            }, i * delayMs, TimeUnit.MILLISECONDS);
        }
    }

    public void setMasterVolume(float db) {
        masterVolumeDb = db;
        muted = (db <= -79f);
        if (activeClip != null) setGain(activeClip, db);
    }
    public float getMasterVolume() {
        return masterVolumeDb;
    }
    public void toggleMute(){
        if(!muted){
            unmutedVolume = masterVolumeDb;
            masterVolumeDb = -80.0f;
            setGain(activeClip,masterVolumeDb);
            muted = true;
            System.out.println("MUTED MUSIC = (toggleMute():MusicPlayer.java)");
        } else {
            masterVolumeDb = unmutedVolume;
            setGain(activeClip,masterVolumeDb);
            muted = false;
            System.out.println("UNMUTED MUSIC = (toggleMute():MusicPlayer.java)");
        }
    }

    private void setGain(Clip clip, float db) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(db);
            }
        } catch (Exception ignored) {}
    }

    public MusicTracks getCurrentTrack() {
        return currentTrack;
    }
}