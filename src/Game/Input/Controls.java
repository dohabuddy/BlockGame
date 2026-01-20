/*
Controls.java enum class outlines the controls that will be used as well as
setting a default key bind to each control. The defaults are set as strings
abstracting the essential information for seamless implementation unrelated
to the choice of graphical renderer.
 */
package Game.Input;

import java.awt.event.KeyEvent;

public enum Controls {
    PAUSE(KeyEvent.VK_ESCAPE),
    MOVE_LEFT(KeyEvent.VK_LEFT),
    MOVE_RIGHT(KeyEvent.VK_RIGHT),
    ROTATE_CLOCKWISE(KeyEvent.VK_X),
    ROTATE_COUNTER_CLOCKWISE(KeyEvent.VK_Z),
    ROTATE_180(KeyEvent.VK_A),
    SOFT_DROP(KeyEvent.VK_DOWN),
    HARD_DROP(KeyEvent.VK_SPACE),
    HOLD(KeyEvent.VK_SHIFT),
    RESTART(KeyEvent.VK_R);

    private final int defaultKeyBind;
    private int currentBind;

    Controls (int defaultKeyBind){
        this.defaultKeyBind = defaultKeyBind;
        this.currentBind = defaultKeyBind;
    }
    public int getDefaultKeyBind(){
        return this.defaultKeyBind;
    }
    public int getCurrentBind(){
        return this.currentBind;
    }
    public void setBind(int newBind){
        this.currentBind = newBind;
    }
    public static String getReadableKeyName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE: return "ESCAPE";
            case KeyEvent.VK_LEFT: return "LEFT ARROW";
            case KeyEvent.VK_RIGHT: return "RIGHT ARROW";
            case KeyEvent.VK_UP: return "UP ARROW";
            case KeyEvent.VK_DOWN: return "DOWN ARROW";
            case KeyEvent.VK_SPACE: return "SPACE BAR";
            case KeyEvent.VK_ENTER: return "ENTER";
            case KeyEvent.VK_SHIFT: return "SHIFT";
            case KeyEvent.VK_R: return "R";
            // Add more as needed
            default: return KeyEvent.getKeyText(keyCode).toUpperCase();
        }
    }
}
