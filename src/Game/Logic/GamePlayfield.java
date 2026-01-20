/*
GamePlayfield.java holds most of the game functionality. This class
creates and maintains the playfield which is used by the output package to render
the board. This class holds a lot of data that probably should have been
moved into separate classes for this class to use.
 */
package Game.Logic;

import Game.Output.MusicTracks;
import Game.Output.SoundEffects;
import Game.Output.SoundPlayer;

import java.util.Map;

public class GamePlayfield {

    public int[][] pieceCoordinates; // 4x2 array [0-3][x, y]
    public int currentPieceLocationX;
    public int currentPieceLocationY;
    public boolean holdIsEmpty;
    public int level;
    public int totalLineClears;
    public int score;
    public int combo;
    public int backToBack;
    public String spinType;
    public boolean perfectClear;
    public boolean pieceLocked;
    public boolean holdQueued;
    public boolean hardDropQueued;

    public int lockDelay = 30;
    public int MAX_LOCK_DELAY = 35; // Default
    private int rotationLockCounter = 0;
    private static final int MAX_ROTATION_LOCKS = 15;

    int playfieldHeight = 23;
    int playfieldWidth = 10;
    int[][] playfield; // 10x23 array [x][y]
    int[][] currentPiece; // 4x4 array - [y][x]
    boolean[][] collisionPlayfield; // 10x23 array [x][y]
    int gravityClock;
    int gravityStep;
    int softDropFactor;
    Shapes pieceOnHold;
    boolean holdLock;
    boolean successfulRotation;
    boolean tSpin;
    boolean bypassMiniCheck;
    boolean wasGroundedBeforeRotation;

    public boolean pieceAcceptingInput;
    private boolean lockCooldownActive = false;
    private long lockCooldownTimer = 0;
    public boolean noPiece;
    private GameManager gameManager;
    private GamePieceManager gamePieceManager;
    private SoundPlayer soundPlayer;

    public enum ROTATION{
        CLOCKWISE(1),
        COUNTER_CLOCKWISE(-1),
        ONE_HUNDRED_EIGHTY(2);
        int rotations;
        ROTATION (int rotations){
            this.rotations = rotations;
        }
    }
    private final Map<Integer, MusicTracks> levelMusicMap = Map.of(
            1, MusicTracks.LEVEL_1,
            5, MusicTracks.LEVEL_5,
            10, MusicTracks.LEVEL_10,
            15, MusicTracks.LEVEL_15,
            20, MusicTracks.LEVEL_20,
            25, MusicTracks.LEVEL_25,
            30, MusicTracks.LEVEL_30,
            35, MusicTracks.LEVEL_35,
            40, MusicTracks.LEVEL_40
    );

    public GamePlayfield(GameManager gameManager, GamePieceManager gamePieceManager, SoundPlayer soundPlayer, int statingLevel){
        this.gameManager = gameManager;
        this.gamePieceManager = gamePieceManager;
        this.playfield = new int[playfieldWidth][playfieldHeight];
        this.level = statingLevel;
        setGravity(level);
        this.noPiece = true;
        this.collisionPlayfield = new boolean[playfieldWidth][playfieldHeight];
        this.pieceCoordinates = new int[4][2];
        resetPieceData();
        this.softDropFactor = gameManager.SDF;
        this.holdIsEmpty = true;
        this.score = 0;
        this.holdQueued = false;
        this.hardDropQueued = false;
        this.soundPlayer = soundPlayer;
        this.lockCooldownActive = false;
    }

    public void stepPlayfield(){

        if (holdQueued) {
            hold();
            holdQueued = false;
            return; // prevent further stepping this tick
        }
        if(hardDropQueued){
            hardDrop();
            hardDropQueued = false;
            return;
        }
        // Handle delayed spawn from lock
        if (lockCooldownActive) {
            if (System.currentTimeMillis() - lockCooldownTimer >= 16) { // ~1 frame at 60fps
                lockCooldownActive = false;
                noPiece = true;
                gamePieceManager.incrementCurrentBagLocation();
                resetPieceData();
            }
            return; // Wait for the cooldown to finish
        }
        if(noPiece) {
            currentPiece = gamePieceManager.getPiece();
            pieceCoordinates = getPieceCoordinates(currentPiece);
            if(!spawnAreaBlocked()){
                spawnPiece();
                pieceAcceptingInput = true;
            } else{
                int counter = 0;
                while (spawnAreaBlocked() && counter < 5) {
                    counter++;
                    if (counter == 4) {
                        soundPlayer.playSound(SoundEffects.GAME_OVER,-20.0f);
                        gameManager.endGame();
                    }
                    gamePieceManager.rotatePieceClockwise();
                    updatePiece();
                }
            }
        } else {
            gravityClock += gravityStep;
            if (gravityClock >= 1600) {
                dropPiece(gravityClock / 1600);
                gravityClock = 0;
            }
            if (isGrounded()) {
                startLockDelay();
            }
        }
    }
    private void clearPiece() {
        for (int index = 0; index < 4; ++index) {
            playfield[getCurrentX(index)][getCurrentY(index)] = 0;
        }
    }
    private boolean checkSoftDropCollision(){
        try{
            for(int i = 0; i < 4; ++i) {
                if (collisionPlayfield[getCurrentX(i)][getCurrentY(i) + 1]) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }
    private boolean checkGravityCollision(int cells){
        try {
            for(int i = 0; i < 4; ++i) {
                if (collisionPlayfield[getCurrentX(i)][getCurrentY(i) + cells]) {
                    return true;
                }
            }
        } catch(ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }

    private int getCurrentX(int pieceCoordinateIndex){
        return pieceCoordinates[pieceCoordinateIndex][0] + currentPieceLocationX;
    }
    private int getCurrentY(int pieceCoordinateIndex){
        return pieceCoordinates[pieceCoordinateIndex][1] + currentPieceLocationY;
    }
    public int[][] getCurrentHoldPiece(){
        return pieceOnHold.rotation.get(0);
    }
    public int[][] getPlayfield(){
        return playfield;
    }
    private int[][] getPieceCoordinates(int[][] piece){
        int[][] coordinateArray = new int[4][2]; // [4 sub blocks][x and y]
        int index = 0;
        for(int x = 0; x < 4; ++x) {
            for (int y = 0; y < 4; ++y) {
                if (piece[x][y] != 0) {
                    coordinateArray[index][1] = x;
                    coordinateArray[index][0] = y; // Swap x and y for consistency
                    index++;
                }
            }
        }
        return coordinateArray;
    }
    private int getPieceNumberIndex(){
        return gamePieceManager.getCurrentShape().ordinal() + 1;
    }
    private synchronized void dropPiece(int cells){
        clearPiece();
        for (int y = 0; y < cells; ++y) {
            if (!checkGravityCollision(1)) {
                currentPieceLocationY++;
            }
        }
        updatePlayfield();
    }
    private synchronized boolean checkCollisionRight(){
        try {
            for(int index = 0; index < 4; ++index) {
                if (collisionPlayfield[getCurrentX(index) + 1][getCurrentY(index)]) {
                    return true;
                }
            }
        } catch(ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }
    public synchronized void movePieceRight(){

        if(!checkCollisionRight()){
            soundPlayer.playSound(SoundEffects.MOVE_RIGHT, -20.0f);
            clearPiece();
            for(int index = 0; index < 4; ++index){
                playfield[getCurrentX(index) + 1][getCurrentY(index)] = getPieceNumberIndex();
            }
            currentPieceLocationX++;
            if (isGrounded()) {
                lockDelay = MAX_LOCK_DELAY;
            }
        }
    }
    private synchronized boolean checkCollisionLeft(){
        try {
            for(int index = 0; index < 4; ++index) {
                if (collisionPlayfield[getCurrentX(index) - 1][getCurrentY(index)]) {
                    return true;
                }
            }
        } catch(ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }
    public synchronized void movePieceLeft(){

        if(!checkCollisionLeft()){
            soundPlayer.playSound(SoundEffects.MOVE_LEFT, -20.0f);
            clearPiece();
            for(int index = 0; index < 4; ++index){
                playfield[getCurrentX(index) - 1][getCurrentY(index)] = getPieceNumberIndex();
            }
            currentPieceLocationX--;
            if (isGrounded()) {
                lockDelay = MAX_LOCK_DELAY;
            }
        }
    }

    public synchronized void rotatePiece(ROTATION direction){

        wasGroundedBeforeRotation = isGrounded();
        int startingRotaion = gamePieceManager.getCurrentRotation();
        boolean collision = checkRotationCollision(direction);
        if(!collision){
            successfulRotation = true;
        }

        clearPiece();
        switch(direction){
            case CLOCKWISE:
                if(collision){
                    pieceCoordinates = attemptKick(startingRotaion, direction);
                    successfulRotation = true;
                } else {
                    gamePieceManager.rotatePieceClockwise();
                    updatePiece();
                    successfulRotation = true;
                }
                break;
            case COUNTER_CLOCKWISE:
                if(collision){
                    pieceCoordinates = attemptKick(startingRotaion, direction);
                    successfulRotation = true;
                } else {
                    gamePieceManager.rotatePieceCounterClockwise();
                    updatePiece();
                    successfulRotation = true;
                }
                break;
            case ONE_HUNDRED_EIGHTY:
                if(collision) {
                    pieceCoordinates = attempt180Kick(startingRotaion);
                    successfulRotation = true;
                } else{
                    gamePieceManager.rotatePiece180();
                    updatePiece();
                    successfulRotation = true;
                }
                break;
            default:
                System.out.println("WARNING WARNING: Invalid Rotation");
        }
        if(wasGroundedBeforeRotation){
            lockDelay = MAX_LOCK_DELAY;
            rotationLockCounter++;
        }
        updatePlayfield();
    }
    private synchronized boolean checkRotationCollision(ROTATION direction){
        try {
            int rotation = gamePieceManager.getCurrentRotation();
            int nextRotation = getNextRotationState(rotation, direction);
            Shapes shape = gamePieceManager.getCurrentShape();
            int[][] rotatedShape = shape.rotation.get(nextRotation);
            int[][] newPieceCoordinates = getPieceCoordinates(rotatedShape);
            for(int i = 0; i < 4; ++i) {
                if (collisionPlayfield[newPieceCoordinates[i][0] + currentPieceLocationX][newPieceCoordinates[i][1] + currentPieceLocationY]) {
                    return true;
                }
            }
        } catch(ArrayIndexOutOfBoundsException e){
            return true;
        }
        return false;
    }
    private int getNextRotationState(int start, ROTATION direction){
        int rotation = start;
        switch (direction){
            case CLOCKWISE:
                if (rotation == 3){
                    rotation = 0;
                } else{
                    rotation++;
                }
                break;
            case COUNTER_CLOCKWISE:
                if (rotation == 0){
                    rotation = 3;
                } else{
                    rotation--;
                }
                break;
            case ONE_HUNDRED_EIGHTY:
                rotation = switch (rotation) {
                    case 0 -> 2;
                    case 1 -> 3;
                    case 2 -> 0;
                    case 3 -> 1;
                    default -> rotation;
                };
        }
        return rotation;
    }
    private synchronized int[][] attemptKick(int startingRotation, ROTATION rotation) {
        Shapes shape = gamePieceManager.getCurrentShape();
        if(shape == Shapes.O) return pieceCoordinates; // O does not kick
        int[][] newPieceCoordinates = new int[4][2];
        int nextRotation = getNextRotationState(startingRotation,rotation);
        int[][] rotatedPiece = shape.rotation.get(nextRotation);
        int[][] testPiece = getPieceCoordinates(rotatedPiece);
        int[][] kickTable = getKickTable(shape, startingRotation, rotation);

        int index = 0;
        while(index < 5){
            boolean valid = true;
            for (int i = 0; i < 4; ++i) {
                int testX = testPiece[i][0] + currentPieceLocationX + kickTable[index][0];
                int testY = testPiece[i][1] + currentPieceLocationY + kickTable[index][1];
                try {
                    if (collisionPlayfield[testX][testY]) {
                        valid = false;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                // Apply kick
                for (int i = 0; i < 4; ++i) {
                    newPieceCoordinates[i][0] = testPiece[i][0];
                    newPieceCoordinates[i][1] = testPiece[i][1];
                }
                currentPieceLocationX += kickTable[index][0];
                currentPieceLocationY += kickTable[index][1];
                switch(rotation){
                    case CLOCKWISE -> gamePieceManager.rotatePieceClockwise();
                    case COUNTER_CLOCKWISE -> gamePieceManager.rotatePieceCounterClockwise();
                }
                successfulRotation = true;
                bypassMiniCheck = false;
                if(index == 4 ){
                    bypassMiniCheck = true;
                }
                return newPieceCoordinates;
            }
            index++;
        }
        return pieceCoordinates;
    }
    private synchronized int[][] attempt180Kick(int startingRotation){
        Shapes shape = gamePieceManager.getCurrentShape();
        if(shape == Shapes.O) return pieceCoordinates; // O does not kick

        int[][] newPieceCoordinates = new int[4][2];
        int nextRotation = getNextRotationState(startingRotation,ROTATION.ONE_HUNDRED_EIGHTY);
        int[][] rotatedPiece = shape.rotation.get(nextRotation);
        int[][] testPiece = getPieceCoordinates(rotatedPiece);
        int[][] kickTable = get180KickTable(shape, startingRotation);

        int index = 0;
        while(index < 5){
            boolean valid = true;
            for (int i = 0; i < 4; ++i) {
                int testX = testPiece[i][0] + currentPieceLocationX + kickTable[index][0];
                int testY = testPiece[i][1] + currentPieceLocationY + kickTable[index][1];
                try {
                    if (collisionPlayfield[testX][testY]) {
                        valid = false;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                // Apply kick
                for (int i = 0; i < 4; ++i) {
                    newPieceCoordinates[i][0] = testPiece[i][0];
                    newPieceCoordinates[i][1] = testPiece[i][1];
                }
                currentPieceLocationX += kickTable[index][0];
                currentPieceLocationY += kickTable[index][1];
                gamePieceManager.rotatePiece180();
                successfulRotation = true;
                return newPieceCoordinates;
            }
            index++;
        }
        return pieceCoordinates;
    }
    private int[][] getKickTable(Shapes shape, int start, ROTATION direction){
        int[][] table = new int[5][2];
        switch(shape){
            case I -> {
                if(start == 0 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -2;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = 0;
                    table[3][0] = -2;
                    table[3][1] = 1;
                    table[4][0] = 1;
                    table[4][1] = -2;
                    return table;
                } else if((start == 0 && direction == ROTATION.COUNTER_CLOCKWISE)){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = 2;
                    table[2][1] = 0;
                    table[3][0] = -1;
                    table[3][1] = -2;
                    table[4][0] = 2;
                    table[4][1] = -1;
                    return table;
                }
                else if(start == 1 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = 2;
                    table[2][1] = 0;
                    table[3][0] = -1;
                    table[3][1] = -2;
                    table[4][0] = 2;
                    table[4][1] = 1;
                    return table;
                }
                else if(start == 1 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 2;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = 0;
                    table[3][0] = 2;
                    table[3][1] = -1;
                    table[4][0] = -1;
                    table[4][1] = 2;
                    return table;
                }
                else if(start == 2 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 2;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = 0;
                    table[3][0] = 2;
                    table[3][1] = -1;
                    table[4][0] = -1;
                    table[4][1] = 2;
                    return table;
                }
                else if(start == 2 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = -2;
                    table[2][1] = 0;
                    table[3][0] = 1;
                    table[3][1] = 2;
                    table[4][0] = -2;
                    table[4][1] = -1;
                    return table;
                }
                else if(start == 3 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = -2;
                    table[2][1] = 0;
                    table[3][0] = 1;
                    table[3][1] = 2;
                    table[4][0] = -2;
                    table[4][1] = -1;
                    return table;
                }
                else if(start == 3 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -2;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = 0;
                    table[3][0] = -2;
                    table[3][1] = 1;
                    table[4][0] = 1;
                    table[4][1] = -2;
                    return table;
                }
            }
            case T,S,Z,J,L ->{
                if(start == 0 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = 2;
                    table[4][0] = -1;
                    table[4][1] = 2;
                    return table;
                } else if((start == 0 && direction == ROTATION.COUNTER_CLOCKWISE)){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = 2;
                    table[4][0] = 1;
                    table[4][1] = 2;
                    return table;
                }
                else if(start == 1 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = 1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = 1;
                    table[4][1] = -2;
                    return table;
                }
                else if(start == 1 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = 1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = 1;
                    table[4][1] = -2;
                    return table;
                }
                else if(start == 2 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = 2;
                    table[4][0] = 1;
                    table[4][1] = 2;
                    return table;
                }
                else if(start == 2 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = 2;
                    table[4][0] = -1;
                    table[4][1] = 2;
                    return table;
                }
                else if(start == 3 && direction == ROTATION.CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = 1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = -1;
                    table[4][1] = -2;
                    return table;
                }
                else if(start == 3 && direction == ROTATION.COUNTER_CLOCKWISE){
                    table[0][0] = 0;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = 1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = -1;
                    table[4][1] = -2;
                    return table;
                }
            }
        }
        return table;
    }
    private int[][] get180KickTable(Shapes shape, int start){
        int[][] table = new int[5][2];
        switch (shape){
            case I -> {
                if(start == 0) {
                    table[0][0] = -1;
                    table[0][1] = 0;
                    table[1][0] = -2;
                    table[1][1] = 0;
                    table[2][0] = 1;
                    table[2][1] = 0;
                    table[3][0] = 2;
                    table[3][1] = 0;
                    table[4][0] = 0;
                    table[4][1] = -1;
                    return table;
                } else if(start == 1){
                    table[0][0] = 0;
                    table[0][1] = -1;
                    table[1][0] = 0;
                    table[1][1] = -2;
                    table[2][0] = -1;
                    table[2][1] = 0;
                    table[3][0] = -2;
                    table[3][1] = 0;
                    table[4][0] = 0;
                    table[4][1] = 1;
                    return table;
                } else if(start == 2){
                    table[0][0] = 1;
                    table[0][1] = 0;
                    table[1][0] = 2;
                    table[1][1] = 0;
                    table[2][0] = -1;
                    table[2][1] = 0;
                    table[3][0] = -2;
                    table[3][1] = 0;
                    table[4][0] = 0;
                    table[4][1] = 1;
                    return table;
                } else if(start == 3){
                    table[0][0] = 0;
                    table[0][1] = -1;
                    table[1][0] = 0;
                    table[1][1] = -2;
                    table[2][0] = 0;
                    table[2][1] = 1;
                    table[3][0] = 0;
                    table[3][1] = 2;
                    table[4][0] = 1;
                    table[4][1] = 0;
                    return table;
                }
            }
            case T,S,Z,J,L -> {
                if (start == 0) {
                    table[0][0] = 0;
                    table[0][1] = 1;
                    table[1][0] = 1;
                    table[1][1] = -1;
                    table[2][0] = -1;
                    table[2][1] = -1;
                    table[3][0] = 1;
                    table[3][1] = 0;
                    table[4][0] = -1;
                    table[4][1] = 0;
                    return table;
                } else if (start == 1) {
                    table[0][0] = 1;
                    table[0][1] = 0;
                    table[1][0] = 1;
                    table[1][1] = -2;
                    table[2][0] = 1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = 0;
                    table[4][1] = -1;
                    return table;
                } else if (start == 2) {
                    table[0][0] = 0;
                    table[0][1] = 1;
                    table[1][0] = -1;
                    table[1][1] = 1;
                    table[2][0] = 1;
                    table[2][1] = 1;
                    table[3][0] = -1;
                    table[3][1] = 0;
                    table[4][0] = 1;
                    table[4][1] = 0;
                    return table;
                } else if (start == 3) {
                    table[0][0] = -1;
                    table[0][1] = 0;
                    table[1][0] = -1;
                    table[1][1] = -2;
                    table[2][0] = -1;
                    table[2][1] = -1;
                    table[3][0] = 0;
                    table[3][1] = -2;
                    table[4][0] = 0;
                    table[4][1] = -1;
                    return table;
                }
            }
        }
        return table;
    }

    private synchronized void startLockDelay() {
        //System.out.println("Lock Delay = " + lockDelay + " | Rotation Lock Counter = " + rotationLockCounter + " startLockDelay():GamePlayfield.java");
        if (!isGrounded()) {
            // Cancel delay if piece isn't grounded
            lockDelay = MAX_LOCK_DELAY;
        } else if (lockDelay <= 0 || rotationLockCounter >= MAX_ROTATION_LOCKS) {
            pieceLock();
        } else {
            lockDelay--;
        }
        //System.out.println("Lock Delay = " + lockDelay + " | Rotation Lock Counter = " + rotationLockCounter + " startLockDelay():GamePlayfield.java");
    }
    private synchronized void resetLockDelay(){
        lockDelay = MAX_LOCK_DELAY;
        rotationLockCounter = 0;
        //System.out.println("Lock Delay = " + lockDelay + " | Rotation Lock Counter = " + rotationLockCounter + " resetLockDelay():GamePlayfield.java");
    }
    private void checkLineClear(){
        int index = 0;
        int[] rowsToClear = new int[4]; // 0-4 lines can be cleared
        for(int row = 0; row < playfieldHeight; ++row){
            int counter = 0;
            int column = 0;
            while(column < playfieldWidth){
                if(!collisionPlayfield[column][row]){
                    break;
                }
                counter++;
                column++;
            }
            if(counter == playfieldWidth){
                rowsToClear[index] = row;
                index++;
            }
        }
        if(index > 0){
            clearLines(index, rowsToClear);
        } else {
            int tSpin = checkTSpinType(0);
            if(tSpin != 0){
                int addScore = 0;
                spinType = switch (tSpin) {
                    case 6 -> "T Spin Mini";
                    case 7 -> "T Spin";
                    default -> "";
                };
                addScore += switch (tSpin){
                    case 6 -> 100 * level;
                    case 7 -> 400 * level;
                    default -> 0;
                };
                gameManager.board.setClearType("Zero", addScore, perfectClear);
                score += addScore;
            }
            combo = 0;
        }
    }
    private boolean threeOccupiedCorners(int centerX, int centerY) {
        int occupied = 0;
        int[][] corners = {
                {centerX - 1, centerY - 1}, // Top Left
                {centerX + 1, centerY - 1}, // Top Right
                {centerX - 1, centerY + 1}, // Bottom Left
                {centerX + 1, centerY + 1}  // Bottom Right
        };
        for (int[] c : corners) {
            int x = c[0];
            int y = c[1];
            if (x < 0 || x >= playfieldWidth || y < 0 || y >= playfieldHeight || collisionPlayfield[x][y]) {
                occupied++;
            }
        }
        return occupied >= 3;
    }
    private boolean isFacingFilledCorners(int centerX, int centerY) {
        int rotation = gamePieceManager.getCurrentRotation(); // assuming rotation is 0=up, 1=right, etc.
        boolean corner1 = false;
        boolean corner2 = false;

        switch (rotation) {
            case 0 -> { // Facing Up
                corner1 = (centerX - 1 < 0 || centerY - 1 < 0 || collisionPlayfield[centerX - 1][centerY - 1]); // Top Left
                corner2 = (centerX + 1 >= playfieldWidth || centerY - 1 < 0 || collisionPlayfield[centerX + 1][centerY - 1]); // Top Right
            }
            case 1 -> { // Facing Right
                corner1 = (centerX + 1 >= playfieldWidth || centerY - 1 < 0 || collisionPlayfield[centerX + 1][centerY - 1]); // Top Right
                corner2 = (centerX + 1 >= playfieldWidth || centerY + 1 >= playfieldHeight || collisionPlayfield[centerX + 1][centerY + 1]); // Bottom Right
            }
            case 2 -> { // Facing Down
                corner1 = (centerX - 1 < 0 || centerY + 1 >= playfieldHeight || collisionPlayfield[centerX - 1][centerY + 1]); // Bottom Left
                corner2 = (centerX + 1 >= playfieldWidth || centerY + 1 >= playfieldHeight || collisionPlayfield[centerX + 1][centerY + 1]); // Bottom Right
            }
            case 3 -> { // Facing Left
                corner1 = (centerX - 1 < 0 || centerY - 1 < 0 || collisionPlayfield[centerX - 1][centerY - 1]); // Top Left
                corner2 = (centerX - 1 < 0 || centerY + 1 >= playfieldHeight || collisionPlayfield[centerX - 1][centerY + 1]); // Bottom Left
            }
        }
        return corner1 && corner2;
    }
    private int checkTSpinType(int numberOfRows){
        int tSpinType = 0;
        int centerX = currentPieceLocationX + 1;
        int centerY = currentPieceLocationY + 1;
        if (gamePieceManager.getCurrentShape() == Shapes.T && successfulRotation && threeOccupiedCorners(centerX,centerY)) {
            boolean isMini = false;

            // T-Spin detection based on facing corners and fin kick
            if (bypassMiniCheck) {
                isMini = false;  // Full T-Spin because fin kick was used
            } else if (!isFacingFilledCorners(centerX, centerY)) {
                if (wasGroundedBeforeRotation) {
                    isMini = true;  // Mini T-Spin
                } else {
                    tSpinType = -1; // Not a T-Spin
                }
            }
            if(tSpinType == -1){
                return 0;
            }
            if (numberOfRows == 1 && isMini) {
                tSpinType = 1; // T-Spin Mini Single
            } else if (numberOfRows == 2 && isMini) {
                tSpinType = 2; // T-Spin Mini Double
            } else if (numberOfRows == 1) {
                tSpinType = 3; // T-Spin Single
            } else if (numberOfRows == 2) {
                tSpinType = 4; // T-Spin Double
            } else if (numberOfRows == 3) {
                tSpinType = 5; // T-Spin Triple
            } else if (numberOfRows == 0 && isMini) {
            tSpinType = 6; // T-Spin Mini No line clear
            } else if (numberOfRows == 0) {
                tSpinType = 7; // T-Spin No line clear
            }
        }
        return tSpinType;
    }
    private void clearLines(int numberOfRows, int[] rowsToClear){
        int tSpinType = checkTSpinType(numberOfRows);
        totalLineClears += numberOfRows;
        for(int index = 0; index < numberOfRows; ++index){
            int rowToClear = rowsToClear[index];
            for(int row = rowToClear; row > 0; --row){
                for(int column = 0; column < playfieldWidth; ++column){
                    playfield[column][row] = playfield[column][row - 1];
                    collisionPlayfield[column][row] = collisionPlayfield[column][row - 1];
                }
            }
            // clear ghost blocks at top
            for(int columns = 0; columns < playfieldWidth; ++columns){
                playfield[columns][0] = 0;
                collisionPlayfield[columns][0] = false;
            }
        }
        soundPlayer.playSound(SoundEffects.LINE_CLEAR,-10.0f);
        String clearType = "";
        spinType = "";
        int addScore;
        if(tSpinType == 0){
            clearType = switch (numberOfRows) {
                case 1 -> "Single";
                case 2 -> "Double";
                case 3 -> "Triple";
                case 4 -> {
                    soundPlayer.playSound(SoundEffects.QUAD_CLEAR,-20.0f);
                    yield "Quad";
                }
                default -> "";
            };
            addScore = switch (numberOfRows) {
                case 1 -> 100 * level;
                case 2 -> 300 * level;
                case 3 -> 500 * level;
                case 4 -> 800 * level;
                default -> 0;
            };
        } else {
            spinType = switch (tSpinType) {
                case 1, 2 -> {
                    soundPlayer.playSound(SoundEffects.TSPIN_MINI,-10.0f);
                    yield "T Spin Mini";
                }
                case 3, 4, 5 -> {
                    soundPlayer.playSound(SoundEffects.TSPIN,-20.0f);
                    yield "T Spin";
                }
                default -> "";
            };
            clearType = switch (tSpinType) {
                case 1, 3 -> "Single";
                case 2, 4 -> "Double";
                case 5 -> "Triple";
                default -> "";
            };
            addScore = switch (tSpinType){
                case 1 -> 200 * level;
                case 2 -> 400 * level;
                case 3 -> 800 * level;
                case 4 -> 1200 * level;
                case 5 -> 1600 * level;
                default -> 0;
            };
        }
        if(clearType.equals("Quad") || tSpinType > 0){
            if(backToBack > 1){
                addScore += addScore/2;
            }
            backToBack++;
        } else {
            backToBack = 0;
        }
        perfectClear = checkPerfectClear();
        if(perfectClear){
            soundPlayer.playSound(SoundEffects.PERFECT_CLEAR,-20.0f);
            if(backToBack >= 1){
                addScore += 3200 * level;
            } else {
                addScore += switch (numberOfRows) {
                    case 1 -> 800 * level;
                    case 2 -> 1200 * level;
                    case 3 -> 1800 * level;
                    case 4 -> 2000 * level;
                    default -> 0;
                };
            }
        }
        addScore += 50 * combo * level;
        gameManager.board.setClearType(clearType, addScore, perfectClear);
        score += addScore;
        combo++;
        updateLevel();
    }
    private boolean checkPerfectClear() {
        int row = 0;
        while (row < playfieldHeight) {
            int col = 0;
            while (col < playfieldWidth) {
                if (collisionPlayfield[col][row]) {
                    return false; // Found a filled cell
                }
                col++;
            }
            row++;
        }
        return true; // No filled cells found
    }
    public synchronized void softDrop() {
        softDropFactor = gameManager.SDF;
        for(int i = 0; i < softDropFactor; ++i){
            if (!checkSoftDropCollision()) {
                clearPiece();
                currentPieceLocationY += 1;
                score += 1; // Add score for soft drop
                updatePlayfield();
            } else {
                clearPiece();
                currentPieceLocationY = getLowestY();
                updatePlayfield();
            }
        }
    }
    public synchronized void hardDrop() {
        clearPiece();
        int distance = getLowestY() - currentPieceLocationY;
        currentPieceLocationY += distance;
        score += distance * 2; // Add score for hard drop
        pieceLock();
    }
    public synchronized void hold(){
        if (!pieceAcceptingInput || pieceLocked) return;
        if(!holdLock){
            //System.out.println("Hold Lock - " + holdLock);
            clearPiece();
            soundPlayer.playSound(SoundEffects.HOLD,-20.0f);
            if(holdIsEmpty){
                //System.out.println("Hold is empty - " + holdIsEmpty);
                pieceOnHold = gamePieceManager.getCurrentShape();
                gamePieceManager.incrementCurrentBagLocation();
                gamePieceManager.incrementPieceQueue();
                holdIsEmpty = false;
            } else {
                //System.out.println("Hold is NOT empty - " + holdIsEmpty);
                Shapes tempPiece = pieceOnHold;
                pieceOnHold = gamePieceManager.getCurrentShape();
                gamePieceManager.setCurrentShape(tempPiece);
            }
            resetPieceData();
            updatePlayfield();
            holdLock = true;
        }
        //System.out.println("Hold Piece - (hold:GamePlayfield.java)");
    }
    private synchronized void pieceLock() {
        soundPlayer.playSound(SoundEffects.PIECE_LOCK, -20.0f);
        gameManager.blockInputFor(10);
        pieceAcceptingInput = false;
        updateCollisionPlayfield();
        updatePlayfield();
        checkLineClear();

        // Delay spawning for 1 frame or a short period
        lockCooldownActive = true;
        lockCooldownTimer = System.currentTimeMillis();
    }
    private synchronized void resetPieceData(){
        resetLockDelay();
        gravityClock = 0;
        currentPieceLocationY = 0;
        currentPieceLocationX = 3;
        gamePieceManager.resetRotation();
        holdLock = false;
        successfulRotation = false;
        tSpin = false;
        bypassMiniCheck = false;
        wasGroundedBeforeRotation = false;
        perfectClear = false;
        updatePiece();
    }
    private synchronized boolean isGrounded() {
        for (int[] block : pieceCoordinates) {
            int x = block[0] + currentPieceLocationX;
            int y = block[1] + currentPieceLocationY + 1;
            if (y >= playfieldHeight || collisionPlayfield[x][y]) {
                return true; // Piece is touching ground or another piece
            }
        }
        return false;
    }
    public synchronized int getLowestY(){
        try{
            int lowestY = currentPieceLocationY;

            while(lowestY < playfieldHeight - 1) {
                lowestY++;
                for (int index = 0; index < 4; ++index) {
                    int testY = pieceCoordinates[index][1] + lowestY;
                    if(testY >= playfieldHeight || collisionPlayfield[getCurrentX(index)][testY]){
                        return lowestY - 1;
                    }
                }
            }
        } catch(ArrayIndexOutOfBoundsException e){
            return playfieldHeight - 1;
        }
        return playfieldHeight - 1;
    }
    public void setGravity(int level){
        switch (level) {
            case 1  -> gravityStep = 27;
            case 2  -> gravityStep = 33;
            case 3  -> gravityStep = 42;
            case 4  -> gravityStep = 53;
            case 5  -> gravityStep = 72;
            case 6  -> gravityStep = 107;
            case 7  -> gravityStep = 160;
            case 8  -> gravityStep = 267;
            case 9  -> gravityStep = 400;
            case 10 -> gravityStep = 800;
            case 11 -> gravityStep = 1600;
            case 12 -> gravityStep = 3200;
            case 13 -> gravityStep = 4800;
            case 14 -> gravityStep = 6400;
            case 15 -> gravityStep = 9600;
            case 16 -> gravityStep = 12800;
            case 17 -> gravityStep = 16000;
            case 18 -> gravityStep = 20800;
            case 19 -> gravityStep = 25600;
            default -> gravityStep = 32000;
        }
        if(level > 20 && level <= 30){
            MAX_LOCK_DELAY = Math.max(10, 30 - (level - 20)); // Decays to 10 by level 30
        } else if (level > 30){
            MAX_LOCK_DELAY = Math.max(MAX_LOCK_DELAY - 1, 1); // Decrease lock delay by 1 until delay == 1
        }
    }
    private boolean spawnAreaBlocked(){
        for(int i = 0; i < 4; ++i) {
            if(collisionPlayfield[getCurrentX(i)][getCurrentY(i)]){
                return true;
            }
        }
        //System.out.println("Spawn Area Clear");
        return false;
    }
    private synchronized void spawnPiece(){
        for(int index = 0; index < 4; ++index){
            playfield[getCurrentX(index)][getCurrentY(index)] = getPieceNumberIndex();
        }
        this.noPiece = false;
        gamePieceManager.incrementPieceQueue();
        pieceAcceptingInput = true;
    }
    private synchronized void updateCollisionPlayfield(){
        for (int index = 0; index < 4; ++index) {
            collisionPlayfield[getCurrentX(index)][getCurrentY(index)] = true;
        }
    }
    private void updateLevel() {
        int newLevel = Math.max(level, (totalLineClears + 10)/10);
        //System.out.println("Current Level = " + newLevel);
        //System.out.println("Current Line Clears = " + totalLineClears);
        if(newLevel != level){
            soundPlayer.playSound(SoundEffects.LEVEL_UP,-19.0f);
            level = newLevel;
            setGravity(level);
        }
        MusicTracks newTrack = levelMusicMap.get(newLevel);
        if (newTrack != null && gameManager.musicPlayer.getCurrentTrack() != newTrack) {
            soundPlayer.playSound(SoundEffects.MUSIC_TRANSITION, -17.0f);
            gameManager.musicPlayer.startWithDelay(newTrack);
        }
    }
    private void updatePiece(){
        currentPiece = gamePieceManager.getPiece();
        pieceCoordinates = getPieceCoordinates(currentPiece);
    }
    private synchronized void updatePlayfield(){
        for (int index = 0; index < 4; ++index) {
            playfield[getCurrentX(index)][getCurrentY(index)] = getPieceNumberIndex();
        }
    }
}