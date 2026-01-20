/*
GameLoop.java extends the Thread class to create an active game loop.
GameManager.java starts and stops the loop in this class
 */
package Game.Logic;

public class GameLoop extends Thread{ // MAYBE NOT RIGHT!!!!!!








    //BE WARNED!!!!!!
    // def not done






    private final GameManager gameManager;
    private final int FPS = 16; // 60 Frames per second (1000 ms)
    private boolean begin;
    private boolean running;
    public boolean countdown;
    private final int ONE_SECOND = 1000;
    private int dasDelay;
    private int arrDelay;

    private boolean pauseQueued;

    private volatile boolean leftInitialPressed = false;
    private volatile boolean rightInitialPressed = false;
    private volatile boolean rotateClockwisePressed = false;
    private volatile boolean rotateCounterClockwisePressed = false;
    private volatile boolean rotate180Pressed = false;
    private volatile boolean softDropPressed = false;
    private volatile boolean hardDropPressed = false;
    private volatile boolean holdPressed = false;

    private volatile long lastLeftPressTime = 0;
    private volatile long lastLeftMoveTime = 0;
    private volatile long lastRightPressTime = 0;
    private volatile long lastRightMoveTime = 0;
    private volatile long lastSoftDropTime = 0;

    private volatile boolean inputBlocked = false;

    public GameLoop(GameManager gameManager){ // Constructor for starting the game
        this.gameManager = gameManager;
        this.begin = true;
        this.countdown = true;
        this.running = true;
        this.dasDelay = gameManager.DAS;
        this.arrDelay = gameManager.ARR;
    }

    public void startThread(){
        System.out.println("GAME STARTED - (startThread:GameLoop.java)");
        //System.out.println("Current Game State - " + gameManager.getCurrentGameState() + " - (startThread:GameLoop.java)");
        start();
    }

    public void pauseThread(){ // Pause loop without ending thread
        System.out.println("GAME PAUSED - (pauseThread:GameLoop.java)");
        //System.out.println("Current Game State - " + gameManager.getCurrentGameState() + " - (pauseThread:GameLoop.java)");
        running = false;
    }
    public void setPauseQueued(){
        pauseQueued = true;
    }
    public void resumeThread(){
        System.out.println("GAME RESUMED - (resumeThread:GameLoop.java)");
        //System.out.println("Current Game State - " + gameManager.getCurrentGameState() + " - (resumeThread:GameLoop.java)");
        countdown = true;
        running = true;
    }

    public void endThread(){
        System.out.println("GAME END - (endThread:GameLoop.java)");
        running = false;
        begin = false;
    }

    @Override
    public void run() {
        try {
            while (begin) {
                while (running) {
                    gameManager.resetFocus();

                    // === Countdown Phase ===
                    while (countdown) {
                        gameManager.board.showCountdown(0); // 0 = "READY?"
                        System.out.println("GAME START IN: READY? - (run:GameLoop.java)");
                        long readyStart = System.currentTimeMillis();
                        while (System.currentTimeMillis() - readyStart < 800) {
                            if (!countdown || !begin) return;
                            Thread.sleep(1);
                        }

                        int countDownCounter = 1;
                        for (int i = 0; i < 4; i++) {
                            if (!countdown || !begin) return;
                            gameManager.countdown(countDownCounter);

                            System.out.println("GAME START IN " + (4 - countDownCounter) + " - (run:GameLoop.java)");

                            long startTime = System.currentTimeMillis();
                            while (System.currentTimeMillis() - startTime < 1000) {
                                if (!countdown || !begin) return;
                                Thread.sleep(1);
                            }
                            countDownCounter++;
                        }

                        if (!countdown || !begin) return;

                        gameManager.countdown(countDownCounter);
                        System.out.println("GAME BEGIN!     - (run:GameLoop.java)");
                        countdown = false;

                        if (pauseQueued) {
                            pauseThread();
                            gameManager.board.openPauseMenu();
                            gameManager.setCurrentGameState(GameState.PAUSED);
                            pauseQueued = false;
                            continue;
                        } else {
                            gameManager.resetFocus();
                            gameManager.setCurrentGameState(GameState.RUNNING);
                        }
                    }

                    long currentTime = System.currentTimeMillis();
                    if (!gameManager.gamePlayField.pieceLocked) handleActions(currentTime);
                    gameManager.stepGame();
                    Thread.sleep(FPS);
                }

                if (!begin) break; // clean exit if thread ended externally
                Thread.sleep(FPS); // idle sleep while paused
            }
        } catch (InterruptedException e) {
            System.err.println("GameLoop interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // preserve interrupt status
        } finally {
            System.out.println("GAME END - (run:GameLoop.java)");
        }
    }
    public void blockInputFor(int ms) {
        inputBlocked = true;
        new Thread(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ignored) {}
            inputBlocked = false;
        }).start();
    }

    public boolean isInputBlocked() {
        return inputBlocked;
    }

    private synchronized void handleActions(long currentTime) {
        if (isInputBlocked() || !gameManager.gamePlayField.pieceAcceptingInput) return;
        if (rotateClockwisePressed) {
            gameManager.gamePlayField.rotatePiece(GamePlayfield.ROTATION.CLOCKWISE);
            rotateClockwisePressed = false;
        }
        if (rotateCounterClockwisePressed) {
            gameManager.gamePlayField.rotatePiece(GamePlayfield.ROTATION.COUNTER_CLOCKWISE);
            rotateCounterClockwisePressed = false;
        }
        if (rotate180Pressed) {
            gameManager.gamePlayField.rotatePiece(GamePlayfield.ROTATION.ONE_HUNDRED_EIGHTY);
            rotate180Pressed = false;
        }

        if (leftInitialPressed && !rightInitialPressed) {
            long timeHeld = currentTime - lastLeftPressTime;
            //System.out.println("[LEFT] Time Held: " + timeHeld + " ms | DAS: " + dasDelay + " ms | ARR: " + arrDelay + " ms");

            if (timeHeld >= dasDelay) {
                long sinceLastMove = currentTime - lastLeftMoveTime;
                //System.out.println("[LEFT] Time since last move: " + sinceLastMove + " ms");

                if (sinceLastMove >= arrDelay) {
                    //System.out.println("[LEFT] MOVE due to ARR");
                    gameManager.gamePlayField.movePieceLeft();
                    lastLeftMoveTime = currentTime;
                }
            } else if (lastLeftMoveTime == 0) {
                //System.out.println("[LEFT] MOVE due to initial press");
                gameManager.gamePlayField.movePieceLeft();
                lastLeftMoveTime = currentTime;
            }
        } else {
            lastLeftPressTime = currentTime;
            lastLeftMoveTime = 0;
        }

        if (rightInitialPressed && !leftInitialPressed) {
            long timeHeld = currentTime - lastRightPressTime;
            //System.out.println("[RIGHT] Time Held: " + timeHeld + " ms | DAS: " + dasDelay + " ms | ARR: " + arrDelay + " ms");

            if (timeHeld >= dasDelay) {
                long sinceLastMove = currentTime - lastRightMoveTime;
                //System.out.println("[RIGHT] Time since last move: " + sinceLastMove + " ms");

                if (sinceLastMove >= arrDelay) {
                    //System.out.println("[RIGHT] MOVE due to ARR");
                    gameManager.gamePlayField.movePieceRight();
                    lastRightMoveTime = currentTime;
                }
            } else if (lastRightMoveTime == 0) {
                //System.out.println("[RIGHT] MOVE due to initial press");
                gameManager.gamePlayField.movePieceRight();
                lastRightMoveTime = currentTime;
            }
        } else {
            lastRightPressTime = currentTime;
            lastRightMoveTime = 0;
        }
        if (softDropPressed) {
            currentTime = System.currentTimeMillis();
            if (lastSoftDropTime == 0 || currentTime - lastSoftDropTime >= arrDelay) {
                gameManager.gamePlayField.softDrop();
                lastSoftDropTime = currentTime;
            }
        } else {
            lastSoftDropTime = 0;
        }
        if (hardDropPressed) {
            gameManager.gamePlayField.hardDropQueued = true;
            gameManager.gamePlayField.pieceAcceptingInput = false; // Disable input after hard drop
            hardDropPressed = false;
        }
        if (holdPressed) {
            gameManager.gamePlayField.holdQueued = true;
            holdPressed = false;
        }
    }

    public void updateTiming() {
        this.dasDelay = gameManager.DAS;
        this.arrDelay = gameManager.ARR;
    }
    public void setLeftInitialPressed(boolean pressed) {
        this.leftInitialPressed = pressed;
    }

    public void setRightInitialPressed(boolean pressed) {
        this.rightInitialPressed = pressed;
    }

    public void setRotateClockwisePressed(boolean pressed) {
        this.rotateClockwisePressed = pressed;
    }

    public void setRotateCounterClockwisePressed(boolean pressed) {
        this.rotateCounterClockwisePressed = pressed;
    }

    public void setRotate180Pressed(boolean pressed) {
        this.rotate180Pressed = pressed;
    }

    public void setSoftDropPressed(boolean pressed) {
        this.softDropPressed = pressed;
    }

    public void setHardDropPressed(boolean pressed) {
        this.hardDropPressed = pressed;
    }

    public void setHoldPressed(boolean pressed) {
        this.holdPressed = pressed;
    }
}
