package Game.Output;

public enum SoundEffects {
    MOVE_LEFT("/Resources/SoundEffects/MOVE_LEFT.wav"),
    MOVE_RIGHT("/Resources/SoundEffects/MOVE_RIGHT.wav"),
    HOLD("/Resources/SoundEffects/HOLD.wav"),
    LINE_CLEAR("/Resources/SoundEffects/LINE_CLEAR.wav"),
    QUAD_CLEAR("/Resources/SoundEffects/QUAD_CLEAR.wav"),
    PERFECT_CLEAR("/Resources/SoundEffects/PERFECT_CLEAR.wav"),
    PIECE_LOCK("/Resources/SoundEffects/PIECE_LOCK.wav"),
    ROTATE_CLOCKWISE("/Resources/SoundEffects/ROTATE_CLOCKWISE.wav"),
    ROTATE_COUNTER_CLOCKWISE("/Resources/SoundEffects/ROTATE_COUNTER_CLOCKWISE.wav"),
    ROTATE_180("/Resources/SoundEffects/ROTATE_180.wav"),
    TSPIN_MINI("/Resources/SoundEffects/TSPIN_MINI.wav"),
    TSPIN("/Resources/SoundEffects/TSPIN.wav"),
    LEVEL_UP("/Resources/SoundEffects/MUSIC_TRANSITION.wav"),
    GAME_OVER("/Resources/SoundEffects/GAME_OVER.wav"),
    COUNTDOWN_3("/Resources/SoundEffects/COUNTDOWN_3.wav"),
    COUNTDOWN_2("/Resources/SoundEffects/COUNTDOWN_2.wav"),
    COUNTDOWN_1("/Resources/SoundEffects/COUNTDOWN_1.wav"),
    COUNTDOWN_BEGIN("/Resources/SoundEffects/COUNTDOWN_BEGIN.wav"),
    MUSIC_TRANSITION("/Resources/SoundEffects/LEVEL_UP.wav"),
    GAME_OPEN("/Resources/SoundEffects/GAME_OPEN.wav"),
    CLICK("/Resources/SoundEffects/CLICK.wav");


    private final String path;

    SoundEffects(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
