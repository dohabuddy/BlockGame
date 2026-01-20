package Game.Input;

import Game.Logic.GameLoop;
import Game.Logic.GameManager;
import Game.Logic.GamePlayfield;
import Game.Logic.GameState;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class PlayerInput implements KeyListener {

    private final Map<Integer, Controls> keyBindings;
    private GameManager gameManager;
    private GamePlayfield gamePlayfield;
    private GameLoop gameLoop;
    private final Map<Controls, Boolean> keyStates = new HashMap<>();

    public PlayerInput(GameManager gameManager, GamePlayfield gamePlayfield, GameLoop gameLoop){
        keyBindings = new HashMap<>();
        for(Controls control : Controls.values()){
            keyBindings.put(control.getCurrentBind(), control);
        }
        this.gameManager = gameManager;
        this.gamePlayfield = gamePlayfield;
        this.gameLoop = gameLoop;
    }
    public boolean isKeyHeld(Controls control) {
        return keyStates.getOrDefault(control, false);
    }
    public void setLeftPressed(boolean pressed) {
        gameLoop.setLeftInitialPressed(pressed);
    }
    public void setRightPressed(boolean pressed) {
        gameLoop.setRightInitialPressed(pressed);
    }
    public void setRotateClockwisePressed(boolean pressed) {
        gameLoop.setRotateClockwisePressed(pressed);
    }
    public void setRotateCounterClockwisePressed(boolean pressed) {
        gameLoop.setRotateCounterClockwisePressed(pressed);
    }
    public void setRotate180Pressed(boolean pressed) {
        gameLoop.setRotate180Pressed(pressed);
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyPressed(KeyEvent e) {
        Controls action = keyBindings.get(e.getKeyCode());
        if (action != null) {
            keyStates.put(action, true);

            switch (action) {
                case MOVE_LEFT:
                    setLeftPressed(true);
                    setRightPressed(false);
                    break;
                case MOVE_RIGHT:
                    setRightPressed(true);
                    setLeftPressed(false);
                    break;
                case SOFT_DROP:
                    gameLoop.setSoftDropPressed(true);
                    break;
                case ROTATE_CLOCKWISE:
                    setRotateClockwisePressed(true);
                    break;
                case ROTATE_COUNTER_CLOCKWISE:
                    setRotateCounterClockwisePressed(true);
                    break;
                case ROTATE_180:
                    setRotate180Pressed(true);
                    break;
                case HARD_DROP:
                    gameLoop.setHardDropPressed(true);
                    break;
                case HOLD:
                    gameLoop.setHoldPressed(true);
                    break;
                case PAUSE:
                    handleAction(action); // Instant actions
                    break;
                case RESTART:
                    handleAction(action);
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        Controls action = keyBindings.get(e.getKeyCode());
        if (action != null) {
            keyStates.put(action, false);

            switch (action) {
                case MOVE_LEFT:
                    setLeftPressed(false);
                    break;
                case MOVE_RIGHT:
                    setRightPressed(false);
                    break;
                case SOFT_DROP:
                    gameLoop.setSoftDropPressed(false);
                    break;
            }
        }
    }
    public void updateBinds() {
        keyBindings.clear(); // Remove all old key mappings
        for (Controls control : Controls.values()) {
            keyBindings.put(control.getCurrentBind(), control);
        }
    }
    private void handleAction(Controls action){
        switch(action){
            case PAUSE -> gameManager.togglePause();
            case ROTATE_CLOCKWISE -> gamePlayfield.rotatePiece(GamePlayfield.ROTATION.CLOCKWISE);
            case ROTATE_COUNTER_CLOCKWISE -> gamePlayfield.rotatePiece(GamePlayfield.ROTATION.COUNTER_CLOCKWISE);
            case ROTATE_180 -> gamePlayfield.rotatePiece(GamePlayfield.ROTATION.ONE_HUNDRED_EIGHTY);
            case SOFT_DROP -> gamePlayfield.softDrop();
            case HARD_DROP -> gamePlayfield.hardDrop();
            case HOLD -> gamePlayfield.hold();
            case RESTART -> gameManager.restartKeyPress();
            default -> System.out.println("INVALID KEY PRESS - (handleAction:PlayerInput.java)");
        }
    }
}
