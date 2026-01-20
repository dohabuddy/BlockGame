package Game.Output;

import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {
    private final Map<SoundEffects, SoundEffect> sounds = new HashMap<>();
    public float soundEffectsVolumeDb = 0.0f; // 0 = normal, negative = quieter
    public float unmutedVolumeDb;
    public boolean masterMuted;

    public SoundPlayer() {
        unmutedVolumeDb = soundEffectsVolumeDb;
        masterMuted = false;
        preload(SoundEffects.MOVE_LEFT, 10);
        preload(SoundEffects.MOVE_RIGHT, 10);
        preload(SoundEffects.HOLD,3);
        preload(SoundEffects.LINE_CLEAR,5);
        preload(SoundEffects.QUAD_CLEAR,5);
        preload(SoundEffects.PERFECT_CLEAR,3);
        preload(SoundEffects.PIECE_LOCK,10);
        preload(SoundEffects.ROTATE_CLOCKWISE,10);
        preload(SoundEffects.ROTATE_COUNTER_CLOCKWISE,10);
        preload(SoundEffects.ROTATE_180,10);
        preload(SoundEffects.TSPIN_MINI,5);
        preload(SoundEffects.TSPIN,5);
        preload(SoundEffects.LEVEL_UP,3);
        preload(SoundEffects.GAME_OVER,3);
        preload(SoundEffects.COUNTDOWN_3,2);
        preload(SoundEffects.COUNTDOWN_2,2);
        preload(SoundEffects.COUNTDOWN_1,2);
        preload(SoundEffects.COUNTDOWN_BEGIN,2);
        preload(SoundEffects.GAME_OPEN,2);
        preload(SoundEffects.MUSIC_TRANSITION,2);
        preload(SoundEffects.CLICK,20);
    }

    private void preload(SoundEffects effect, int copies) {
        sounds.put(effect, new SoundEffect(effect.getPath(), copies));
    }

    public void playSound(SoundEffects effect, float baseVolume) {
        if (masterMuted) return;

        SoundEffect sound = sounds.get(effect);
        if (sound != null) {
            // Let each sound decide its default volume and scale it from there
            float adjustedVolume = Math.max(-80f, baseVolume + soundEffectsVolumeDb);
            sound.play(adjustedVolume);
        }
    }
    public void toggleMute(){
        if(!masterMuted){
            unmutedVolumeDb = soundEffectsVolumeDb;
            soundEffectsVolumeDb = -80.0f;
            masterMuted = true;

            System.out.println("MUTED SOUND EFFECTS = (toggleMute():SoundPLayer.java)");
        } else {
            soundEffectsVolumeDb = unmutedVolumeDb;
            masterMuted = false;
            System.out.println("UNMUTED SOUND EFFECTS = (toggleMute():SoundPLayer.java)");
        }
    }
    public void setVolume(float db) {
        soundEffectsVolumeDb = db;
        masterMuted = (db <= -79f);
    }
    public void stopSound(SoundEffects effect){
        if (masterMuted) return;

        SoundEffect sound = sounds.get(effect);
        if (sound != null) {
            sound.stopAll();
        }
    }
}