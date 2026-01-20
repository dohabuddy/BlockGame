/*
The GameManager class controls the essential game logic and provides methods needed
by other classes to determine what the program will do and display next.
*/
package Game.Logic;

import Game.Input.PlayerInput;
import Game.Output.*;

import javax.swing.*;
import java.awt.*;


public class GameManager {

    public GameState currentGameState; // Current game state variable
    private GameLoop loopThread;
    private GamePieceManager gamePieceManager;
    public GamePlayfield gamePlayField;
    private MainMenu mainMenu;
    public Board board;
    private PlayerInput playerInput;
    public SoundPlayer soundPlayer;
    public MusicPlayer musicPlayer;
    public int DAS;
    public int ARR;
    public int SDF;
    public int musicPercent; // in percent
    public int soundEffectsPercent;
    private int frameWidth;
    private int frameHeight;
    private boolean pressedRestart;

    public GameManager() { // Constructor
        this.currentGameState = GameState.MAIN_MENU;
        setDAS(150);
        setARR(50);
        setSDF(1);
        this.soundPlayer = new SoundPlayer();
        soundEffectsPercent = 100;
        soundPlayer.setVolume(0.0f);
        this.musicPlayer = new MusicPlayer();
        musicPercent = 100;
        musicPlayer.setMasterVolume(-5.0f);
        this.mainMenu = new MainMenu(this);
        this.frameWidth = mainMenu.getFrameWidth();
        this.frameHeight = mainMenu.getFrameHeight();

        musicPlayer.startWithDelay(MusicTracks.MAIN_MENU);

    }
//    private JFrame masterFrame;
//    private CardLayout cardLayout;
//    private JPanel cardPanel;
//    public void init() {
//        masterFrame = new JFrame("Block Game");
//        masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        masterFrame.setSize(1000, 800);
//        masterFrame.setLocationRelativeTo(null);
//
//        cardLayout = new CardLayout();
//        cardPanel = new JPanel(cardLayout);
//
//        // Add your views
//        cardPanel.add(mainMenu, "MainMenu");
//        cardPanel.add(board, "Board");
//
//        masterFrame.setContentPane(cardPanel);
//        masterFrame.setVisible(true);
//
//        // Start on main menu
//        cardLayout.show(cardPanel, "MainMenu");
//    }

    public GameState getCurrentGameState() { // Return current game state
        return this.currentGameState;
    }
    public void setCurrentGameState(GameState newState) { // Set current game state
        this.currentGameState = newState;
    }

    public void openMainMenu(){
        this.currentGameState = GameState.MAIN_MENU;
        mainMenu.openMainMenu();
        board.boardFrame.dispose();
    }
    public void openSettingsMenu(){
        switch(getCurrentGameState()){
            case MAIN_MENU:
                mainMenu.openSettingsMenu();
                break;
            case PAUSED, GAME_OVER:
                board.openSettingsMenu();
                break;
            default:
                System.out.println("INVALID STATE TO ACCESS SETTINGS");
        }
    }

    public void startGame(){

        this.gamePieceManager = new GamePieceManager(soundPlayer);
        this.gamePlayField = new GamePlayfield(this, gamePieceManager, soundPlayer, mainMenu.getStartLevel());
        this.loopThread = new GameLoop(this);
        this.playerInput = new PlayerInput(this, gamePlayField, loopThread);
        this.board = new Board(this, gamePlayField, gamePieceManager, frameWidth, frameHeight);
        mainMenu.closeMainMenu();
        soundPlayer.playSound(SoundEffects.MUSIC_TRANSITION,-15.0f);
        setCurrentGameState(GameState.COUNTDOWN);
        board.addKeyListener(playerInput);
        loopThread.startThread();
        switch (mainMenu.getStartLevel()) {
            case 1, 2, 3, 4 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_1);
            case 5, 6, 7, 8, 9 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_5);
            case 10, 11, 12, 13, 14 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_10);
            case 15, 16, 17, 18, 19 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_15);
            case 20, 21, 22, 23, 24 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_20);
            case 25, 26, 27, 28, 29 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_25);
            case 30 -> musicPlayer.startWithDelay(MusicTracks.LEVEL_30);
        }
    }
    public void restartGame() {
        board.hideGameOver();
        board.closePauseMenu();
        board.removeKeyListener(playerInput);

        this.gamePieceManager = new GamePieceManager(soundPlayer);
        this.gamePlayField = new GamePlayfield(this, gamePieceManager, soundPlayer, mainMenu.getStartLevel());
        this.loopThread = new GameLoop(this);
        this.playerInput = new PlayerInput(this, gamePlayField, loopThread);

        board.resetBoard(gamePlayField, gamePieceManager);
        setCurrentGameState(GameState.COUNTDOWN);
        board.addKeyListener(playerInput);
        loopThread.startThread();
        MusicTracks newTrack = switch (mainMenu.getStartLevel()) {
            case 1, 2, 3, 4 -> MusicTracks.LEVEL_1;
            case 5, 6, 7, 8, 9 -> MusicTracks.LEVEL_5;
            case 10, 11, 12, 13, 14 -> MusicTracks.LEVEL_10;
            case 15, 16, 17, 18, 19 -> MusicTracks.LEVEL_15;
            case 20, 21, 22, 23, 24 -> MusicTracks.LEVEL_20;
            case 25, 26, 27, 28, 29 -> MusicTracks.LEVEL_25;
            case 30 -> MusicTracks.LEVEL_30;
            default -> null;
        };

        if (newTrack != null && newTrack != musicPlayer.getCurrentTrack()) {
            musicPlayer.startWithDelay(newTrack);
        }
    }
    public void pauseRestart(){
        pressedRestart = true;
        endGame();
        restartGame();
    }
    public void restartKeyPress(){
        if(currentGameState == GameState.COUNTDOWN){
            loopThread.countdown = false;
        }
        pauseRestart();
    }
    public void togglePause(){
        if(currentGameState == GameState.RUNNING || currentGameState == GameState.COUNTDOWN){
            pauseGame();
        } else if (currentGameState == GameState.PAUSED){
            resumeGame();
        }
    }
    public void pauseGame(){
        if(currentGameState == GameState.RUNNING){
            board.openPauseMenu();
            setCurrentGameState(GameState.PAUSED);
            loopThread.pauseThread();
        } else {
            System.out.println("Game not running yet, queue pause");
            loopThread.setPauseQueued();
        }
    }
    public void resumeGame(){
        board.closePauseMenu();
        updatePlayerInput();
        resetFocus();
        setCurrentGameState(GameState.COUNTDOWN);
        loopThread.resumeThread();
    }
    public void backToMainMenu(){
        loopThread.endThread();
        setCurrentGameState(GameState.MAIN_MENU);
        openMainMenu();
    }
    public void endGame(){
        loopThread.endThread();
        board.endGame();
        setCurrentGameState(GameState.GAME_OVER);
        if(pressedRestart){
            pressedRestart = false;
        } else{
            musicPlayer.startWithDelay(MusicTracks.GAME_OVER);
        }
    }
    public void showGameOver(){
        board.showGameOver();
    }
    public void quit(){
        System.exit(0);
    }
    public void blockInputFor(int ms) {
        loopThread.blockInputFor(ms);
    }
    public void toggleMuteMusic() {
        musicPlayer.toggleMute();
        if (musicPlayer.muted) {
            musicPercent = 0;
        } else {
            float db = musicPlayer.unmutedVolume;
            musicPercent = (int) ((db - (-80f)) / (-5f - (-80f)) * 100);  // Linear scale to 0–100
        }
        board.settingsMenu.musicVolumeSlider.setValue(musicPercent);
        board.updateButtons();
    }
    public void toggleMuteSoundEffects() {
        soundPlayer.toggleMute();
        if (soundPlayer.masterMuted) {
            soundEffectsPercent = 0;
        } else {
            float db = soundPlayer.unmutedVolumeDb;
            soundEffectsPercent = (int) ((db - (-80f)) / (0f - (-80f)) * 100);  // Linear scale to 0–100
        }
        board.settingsMenu.soundVolumeSlider.setValue(soundEffectsPercent);
        board.updateButtons();
    }
    public void updatePlayerInput(){
        if(currentGameState != GameState.MAIN_MENU){
            playerInput.updateBinds();
            setSDF(board.settingsMenu.getSDF());
            loopThread.updateTiming();

        }
    }
    public void stepGame(){
        //System.out.println("GAME STEP          - (stepGame:GameManger.java)");
        gamePlayField.stepPlayfield();
        board.updateBoard();
    }
    public void resetFocus(){
        board.setFocusable(true);
        board.requestFocus();
    }

    public void countdown(int counter){
        board.showCountdown(counter);
        resetFocus();
    }
    public void setDAS(int newDAS){
        DAS = newDAS;
    }
    public void setARR(int newARR){
        ARR = newARR;
    }
    public void setSDF(int newSDF){
        SDF = newSDF;
    }
    public void setMusicVolume(int percent) {
        musicPercent = percent;
    }

    public void setSoundVolume(int percent) {
        soundEffectsPercent = percent;
    }
}
