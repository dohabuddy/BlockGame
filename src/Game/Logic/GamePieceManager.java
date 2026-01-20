/*
GamePieceManager.java controls the logic for game piece spawning. The bag system is also stored in this class and
a map to the shapes class.
 */
package Game.Logic;

import Game.Output.SoundEffects;
import Game.Output.SoundPlayer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class GamePieceManager {

    public int[] currentBag; // in use 7 bag
    public int[] nextBag; // new 7 bag
    private int currentBagLocation; // 0-6
    private int currentRotation; // 0-3
    private SoundPlayer soundPlayer;
    public LinkedList<Integer> pieceQueue; // 5 pieces
    public final Map<Integer, Shapes> SHAPE_MAP = Map.of(
            1, Shapes.I,
            2, Shapes.T,
            3, Shapes.O,
            4, Shapes.Z,
            5, Shapes.S,
            6, Shapes.J,
            7, Shapes.L
    );
    public GamePieceManager(SoundPlayer soundPlayer){
        this.currentBag = newBag();
        this.currentBagLocation = 0;
        this.currentRotation = 0;
        this.nextBag = newBag();
        this.pieceQueue = new LinkedList<>();
        setPieceQueue();
        this.soundPlayer = soundPlayer;
    }
    public Shapes getCurrentShape(){
        return SHAPE_MAP.get(currentBag[currentBagLocation]);
    }
    public int getCurrentRotation(){
        return currentRotation;
    }
    public void setCurrentShape(Shapes shape){
        currentBag[currentBagLocation] = shape.ordinal() + 1;
    }
    public int[][] getShapeArray(Shapes shape){
        switch(shape){
            case I:
                return Shapes.I.rotation.get(0);
            case T:
                return Shapes.T.rotation.get(0);
            case O:
                return Shapes.O.rotation.get(0);
            case Z:
                return Shapes.Z.rotation.get(0);
            case S:
                return Shapes.S.rotation.get(0);
            case J:
                return Shapes.J.rotation.get(0);
            case L:
                return Shapes.L.rotation.get(0);
        }
        System.out.println("WARNING: Invalid Shape Array");
        return null;
    }

    public void rotatePieceClockwise(){
        soundPlayer.playSound(SoundEffects.ROTATE_CLOCKWISE,-20.0f);
        switch(currentRotation){
            case 0, 1, 2:
                currentRotation++;
                break;
            case 3:
                currentRotation = 0;
                break;
        }
    }
    public void rotatePieceCounterClockwise(){
        soundPlayer.playSound(SoundEffects.ROTATE_COUNTER_CLOCKWISE,-20.0f);
        switch(currentRotation){
            case 3, 2, 1:
                currentRotation--;
                break;
            case 0:
                currentRotation = 3;
                break;
        }
    }
    public void rotatePiece180(){
        soundPlayer.playSound(SoundEffects.ROTATE_180,-20.0f);
        switch(currentRotation){
            case 0:
                currentRotation = 2;
                break;
            case 1:
                currentRotation = 3;
                break;
            case 2:
                currentRotation = 0;
                break;
            case 3:
                currentRotation = 1;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currentRotation);
        }
    }
    public void incrementCurrentBagLocation(){
        currentBagLocation++;
    }
    public void resetRotation(){
        currentRotation = 0;
    }
    public int[][] getPiece(){

        //System.out.println("\nNEXT PIECE                - (getNextPiece:GamePieceManger.java)");
        if(currentBagLocation > 6) {
            currentBag = nextBag;
            nextBag = newBag();
            currentBagLocation = 0;
        }
//        System.out.println("CURRENT BAG LOCATION <= 6  - (getNextPiece:GamePieceManager.java)" +
//                "\nCurrent Bag Location = " + currentBagLocation +
//                "\nCurrent Bag = " + Arrays.toString(currentBag) +
//                "\nNext Bag = " + Arrays.toString(nextBag) + "\n");
        Shapes piece = SHAPE_MAP.get(currentBag[currentBagLocation]);
        return piece.rotation.get(currentRotation);
    }
    public synchronized int[] getPieceQueue(){
        return pieceQueue.stream().mapToInt(Integer::intValue).toArray();
    }
    private void setPieceQueue(){
        pieceQueue.add(currentBag[0]);
        pieceQueue.add(currentBag[1]);
        pieceQueue.add(currentBag[2]);
        pieceQueue.add(currentBag[3]);
        pieceQueue.add(currentBag[4]);
        System.out.println("Starting Piece Queue:" + pieceQueue);
    }
    public void incrementPieceQueue(){
//        System.out.println("Current Bag Location (Increment Hold Queue): " + currentBagLocation);
//        System.out.println("Starting Hold Queue:" + pieceQueue);
        pieceQueue.pop();
        //System.out.println("Pop Hold Queue:" + pieceQueue);
        switch (currentBagLocation){
            case 0, 1:
                pieceQueue.add(currentBag[currentBagLocation + 5]);
                //System.out.println("Add Hold Queue (Current Bag Location 0 & 1):" + pieceQueue);
                break;
            case 2, 3, 4, 5, 6:
                pieceQueue.add(nextBag[currentBagLocation - 2]);
                //System.out.println("Add Hold Queue (Current Bag Location 2-6):" + pieceQueue);
                break;
            default:
                //System.out.println("WARNING: Increment Hold Queue out of bounds");
        }
    }

    public int[] newBag(){
        int[] bag = new int[]{1,2,3,4,5,6,7};
        Random randomNumber = new Random();
        for (int i = bag.length - 1; i > 0; i--) {
            int j = randomNumber.nextInt(i + 1);
            int temp = bag[i];
            bag[i] = bag[j];
            bag[j] = temp;
        }
        return bag;
    }
}
