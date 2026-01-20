package Game.Output;

import Game.Logic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class Board extends JPanel{

    private DisplayMaker displayMaker;
    private GameManager gameManager;
    private GamePlayfield gamePlayfield;
    private GamePieceManager gamePieceManager;
    public SettingsMenu settingsMenu;
    private PauseMenu pauseMenu;
    private GameOver gameOver;
    private JButton muteMusic;
    private JButton muteSoundEffects;

    public JFrame boardFrame;
    public int[][] board = new int[10][23]; // column by row

    private int width;
    private int height;
    private int[][] playfieldArray;
    private String clearType = "";
    private long clearTypeTimestamp = 0;
    private static final int CLEAR_TYPE_DISPLAY_MS = 2500;
    private int scoreFlashAmount = 0;
    private boolean perfectClear;
    private boolean fullRainbow;

    private Image backgroundGifImage;
    private int backgroundWidth;
    private int backgroundHeight;

    public Board(GameManager gameManager, GamePlayfield gamePlayfield, GamePieceManager gamePieceManager, int startingWidth, int startingHeight){
        super(true);
        this.displayMaker = new DisplayMaker(gameManager.soundPlayer);
        this.gameManager = gameManager;
        this.width = startingWidth;
        this.height = startingHeight;
        this.boardFrame = makeFrame();
        this.gamePlayfield = gamePlayfield;
        this.playfieldArray = gamePlayfield.getPlayfield();
        this.gamePieceManager = gamePieceManager;
        this.settingsMenu = new SettingsMenu(gameManager, boardFrame.getLayeredPane(), startingWidth, startingHeight);
        this.pauseMenu = new PauseMenu(gameManager, boardFrame.getLayeredPane(), width, height, settingsMenu);
        initializeCountdownLabel(boardFrame.getLayeredPane(),width,height);
        TextureManager.loadTextures();
        // Load background GIF
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/Resources/Textures/BACKGROUND.gif"));
        backgroundGifImage = backgroundIcon.getImage();
        backgroundWidth = backgroundIcon.getIconWidth();
        backgroundHeight = backgroundIcon.getIconHeight();
    }
    public void updateBoard(){
        playfieldArray = gamePlayfield.getPlayfield();
        for(int row = 0; row < playfieldArray.length; ++row){
            for(int column = 0; column < playfieldArray[0].length; ++column){
                board[row][column] = playfieldArray[row][column];
            }
        }
        repaint();
    }
    public void openPauseMenu(){
        pauseMenu.showMenu();
    }
    public void closePauseMenu(){
        pauseMenu.closePauseMenu();
    }

    public void openSettingsMenu(){
        pauseMenu.showSettingsMenu();
    }
    public void endGame(){
        this.gameOver = new GameOver(gameManager, boardFrame.getLayeredPane(), width, height);
    }
    public void showGameOver(){
        pauseMenu.settingsMenuIsOpen = false;
        gameOver.setMenuVisible();
    }
    public void hideGameOver(){
        gameOver.hideMenu();
    }
    public void resetBoard(GamePlayfield gamePlayfield, GamePieceManager gamePieceManager) {
        this.gamePlayfield = gamePlayfield;
        this.gamePieceManager = gamePieceManager;
        this.playfieldArray = gamePlayfield.getPlayfield();
        clearType = "";
        scoreFlashAmount = 0;
        perfectClear = false;
        updateBoard(); // optional, clear visuals
        repaint();
    }

    public void setClearType(String type, int scoreGained, boolean perfectClear) {
        this.clearType = type;
        this.clearTypeTimestamp = System.currentTimeMillis();
        this.scoreFlashAmount = scoreGained;
        this.perfectClear = perfectClear;
        if(perfectClear && clearType.equals("Quad")) fullRainbow = true;
    }
    private JLabel countdownLabel;

    public void initializeCountdownLabel(JLayeredPane layeredPane, int width, int height) {
        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Rockwell", Font.BOLD, height/10));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setBounds(0, 0, width, height/2);
        countdownLabel.setFocusable(false);
        countdownLabel.setVisible(false);

        layeredPane.add(countdownLabel, JLayeredPane.POPUP_LAYER);
        layeredPane.setFocusable(false);
    }
    private void resizeCountdown(int newWidth, int newHeight){
        countdownLabel.setBounds(0, 0, newWidth, newHeight/2);
        countdownLabel.setFont(new Font("Rockwell", Font.BOLD, newHeight/10));
        countdownLabel.repaint();
    }
    public void showCountdown(int counter) {
        if (counter > 4) {
            countdownLabel.setVisible(false);
            boardFrame.requestFocus();
            return;
        }
        String text = switch (counter) {
            case 0 -> "READY?";
            case 1 -> {
                gameManager.soundPlayer.playSound(SoundEffects.COUNTDOWN_3, 0.0f);
                yield "3";
            }
            case 2 -> {
                gameManager.soundPlayer.playSound(SoundEffects.COUNTDOWN_2, 0.0f);
                yield "2";
            }
            case 3 -> {
                gameManager.soundPlayer.playSound(SoundEffects.COUNTDOWN_1, 0.0f);
                yield "1";
            }
            case 4 -> {
                gameManager.soundPlayer.playSound(SoundEffects.COUNTDOWN_BEGIN, 0.0f);
                yield "Begin!";
            }
            default -> "";
        };

        countdownLabel.setText(text);
        countdownLabel.setVisible(true);
    }
    Color[] rainbow = new Color[] {

            new Color(128, 255, 0),  // yellow to green
            Color.GREEN,
            new Color(0, 255, 128),  // green to cyan
            Color.BLUE,
            new Color(38, 0, 128),   // blue to indigo
            new Color(75, 0, 130),   // indigo
            new Color(111, 0, 170),  // indigo to violet
            new Color(148, 0, 211),  // violet
            new Color(255, 0, 128),  // violet to red (wrap)
            Color.RED,
            new Color(255, 64, 0),   // between red and orange
            Color.ORANGE,
            new Color(255, 200, 0),  // between orange and yellow
            Color.YELLOW,
    };
    Color[] purpleRainbow = new Color[] {
            new Color(75, 0, 130),   // indigo
            new Color(111, 0, 170),  // indigo to violet
            new Color(130, 0, 255),
            new Color(170, 0, 255),
            new Color(200, 0, 255),
            new Color(255, 0, 255),
            new Color(255, 0, 200),
            new Color(255, 0, 170),
            new Color(255, 0, 140),
    };
    private JFrame makeFrame(){
        int frameWidth = width;
        int frameHeight = height;
        JFrame boardFrame = displayMaker.frameMaker("Block Game",
                DisplayMaker.closeOperationEnum.EXIT,
                frameWidth,
                frameHeight,
                true,
                DisplayMaker.layoutEnum.CARD);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth,frameHeight));
        layeredPane.add(this,JLayeredPane.DEFAULT_LAYER);
        boardFrame.setContentPane(layeredPane);

        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> gameManager.pauseGame());
        pauseButton.setFocusable(false);
        muteMusic = new JButton("<X");
        muteMusic.addActionListener(e -> gameManager.toggleMuteMusic());
        muteMusic.setFocusable(false);
        muteSoundEffects = new JButton("<X");
        muteSoundEffects.addActionListener(e -> gameManager.toggleMuteSoundEffects());
        muteSoundEffects.setFocusable(false);

        layeredPane.add(pauseButton,JLayeredPane.PALETTE_LAYER);
        layeredPane.add(muteMusic,JLayeredPane.PALETTE_LAYER);
        layeredPane.add(muteSoundEffects,JLayeredPane.PALETTE_LAYER);


        boardFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e){
                System.out.println("RESIZING - makeFrame():Board.java");
                int newWidth = boardFrame.getWidth();
                int newHeight = boardFrame.getHeight();
                width = newWidth;
                height = newHeight;
                int buttonSize = newHeight/20;
                setBounds(0,0, newWidth,newHeight);

                pauseButton.setBounds(buttonSize, newHeight - buttonSize * 3, buttonSize, buttonSize);
                ImageIcon icon = new ImageIcon(getClass().getResource("/Resources/Textures/PAUSE_BUTTON.png"));
                Image scaled = icon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
                pauseButton.setIcon(new ImageIcon(scaled));
                pauseButton.setText(null); // Make sure no text is interfering
                pauseButton.setBorder(null); // Optional: remove border
                pauseButton.setContentAreaFilled(false); // Optional: makes it transparent
                pauseButton.setFocusPainted(false); // Optional: removes focus outline
                pauseButton.setOpaque(false); // Optional


                muteMusic.setBounds((int)(buttonSize * 2.5), newHeight - buttonSize * 3, buttonSize, buttonSize);
                String musicIconPath = gameManager.musicPlayer.muted
                        ? "/Resources/Textures/MUSIC_BUTTON_OFF.png"
                        : "/Resources/Textures/MUSIC_BUTTON_ON.png";

                ImageIcon musicIcon = new ImageIcon(getClass().getResource(musicIconPath));
                Image musicImg = musicIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
                muteMusic.setIcon(new ImageIcon(musicImg));
                muteMusic.setText(null);
                muteMusic.setBorder(null);
                muteMusic.setContentAreaFilled(false);
                muteMusic.setFocusPainted(false);
                muteMusic.setOpaque(false);

                muteSoundEffects.setBounds(buttonSize * 4, newHeight - buttonSize * 3, buttonSize, buttonSize);
                String sfxIconPath = gameManager.soundPlayer.masterMuted
                        ? "/Resources/Textures/SOUND_EFFECTS_BUTTON_OFF.png"
                        : "/Resources/Textures/SOUND_EFFECTS_BUTTON_ON.png";

                ImageIcon sfxIcon = new ImageIcon(getClass().getResource(sfxIconPath));
                Image sfxImg = sfxIcon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
                muteSoundEffects.setIcon(new ImageIcon(sfxImg));
                muteSoundEffects.setText(null);
                muteSoundEffects.setBorder(null);
                muteSoundEffects.setContentAreaFilled(false);
                muteSoundEffects.setFocusPainted(false);
                muteSoundEffects.setOpaque(false);

                pauseMenu.resizePauseMenu(newWidth,newHeight);
                settingsMenu.resizeSettingsMenu();

                resizeCountdown(newWidth,newHeight);
                if(gameManager.currentGameState == GameState.GAME_OVER){
                    gameOver.resizePanels(newWidth,newHeight);
                }
                boardFrame.requestFocus();
                boardFrame.revalidate();
                boardFrame.repaint();
            }
        });
        boardFrame.setVisible(true);
        boardFrame.requestFocus();

        return boardFrame;
    }
    public void updateButtons() {
        // Music Icon
        String musicIconPath = gameManager.musicPlayer.muted
                ? "/Resources/Textures/MUSIC_BUTTON_OFF.png"
                : "/Resources/Textures/MUSIC_BUTTON_ON.png";

        ImageIcon musicIcon = new ImageIcon(getClass().getResource(musicIconPath));
        Image musicImg = musicIcon.getImage().getScaledInstance(height/20, height/20, Image.SCALE_SMOOTH);
        muteMusic.setIcon(new ImageIcon(musicImg));

        // Sound Effects Icon
        String sfxIconPath = gameManager.soundPlayer.masterMuted
                ? "/Resources/Textures/SOUND_EFFECTS_BUTTON_OFF.png"
                : "/Resources/Textures/SOUND_EFFECTS_BUTTON_ON.png";

        ImageIcon sfxIcon = new ImageIcon(getClass().getResource(sfxIconPath));
        Image sfxImg = sfxIcon.getImage().getScaledInstance(height/20, height/20, Image.SCALE_SMOOTH);
        muteSoundEffects.setIcon(new ImageIcon(sfxImg));
    }
    @Override
    public void paintComponent(Graphics G){
        super.paintComponent(G);
        if (fullRainbow && System.currentTimeMillis() - clearTypeTimestamp >= CLEAR_TYPE_DISPLAY_MS) {
            fullRainbow = false;
        }

        // Draw the background
        if (backgroundGifImage != null) {
            G.drawImage(backgroundGifImage, 0, 0, getWidth(), getHeight(), this);
        }
        setBackground(Color.BLACK); // Set Background color



        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int bufferHeight = frameHeight/8;
        int boardHeight = frameHeight - bufferHeight * 2;
        int boardWidth = boardHeight/2; // Half the height

        int xOffSet = (frameWidth - boardWidth)/2; // Center grid
        int yOffSet = (frameHeight - boardHeight)/2;

        int cellSize = boardHeight/20;

        // Paint playfield and grid
        for(int columns = 0; columns < 10; ++columns){
            for(int rows = 0; rows < 20; ++rows) {
                G.setColor(new Color(0, 0, 0, 200)); // Slight transparent black
                G.fillRect(xOffSet + columns * cellSize, yOffSet + rows * cellSize, cellSize, cellSize);
                G.setColor(new Color(20, 20, 20));
                G.drawRect(xOffSet + columns * cellSize, yOffSet + rows * cellSize, cellSize, cellSize);
            }
        }

        // Paint pieces in play
        int boardOffset = yOffSet - cellSize * 3; // Shift pieces up three rows to spawn above the board playfield
        for(int column = 0; column < board.length; ++column){
            for(int row = 0; row < board[0].length; ++row){
                if(board[column][row] != 0) {
                    ImageIcon texture = TextureManager.getTexture(board[column][row]);
                    if (texture != null) {
                        Image img = texture.getImage();
                        G.drawImage(img, xOffSet + column * cellSize, boardOffset + row * cellSize, cellSize, cellSize, this);
                    } else {
                        // fallback to color fill
                        G.setColor(ColorMap.COLOR_MAP.get(board[column][row]));
                        G.fillRect(xOffSet + column * cellSize, boardOffset + row * cellSize, cellSize, cellSize);
                        G.setColor(Color.DARK_GRAY);
                        G.drawRect(xOffSet + column * cellSize, boardOffset + row * cellSize, cellSize, cellSize);
                    }
                }
            }
        }
        // Paint queue box
        int piecesVisibleInQueue = settingsMenu.getNumberOfPiecesVisibleInQueue();
        if(piecesVisibleInQueue > 0){
            G.setFont(new Font("Rockwell", Font.BOLD, cellSize));
            if (fullRainbow) {
                long elapsed = System.currentTimeMillis() - clearTypeTimestamp;
                int stage = (int) ((elapsed / 75) % rainbow.length);
                G.setColor(rainbow[stage]);
            } else {
                G.setColor(Color.WHITE);
            }
            G.drawString("Queue", xOffSet + 12 * cellSize, yOffSet - cellSize / 2);
            G.drawRect(xOffSet + 11 * cellSize , yOffSet, 6 * cellSize, piecesVisibleInQueue * 3 * cellSize - cellSize * 2);
            // Fill queue background
            G.setColor(new Color(0, 0, 0, 180)); // Slight transparent black
            G.fillRect(xOffSet + 11 * cellSize, yOffSet, 6 * cellSize, piecesVisibleInQueue * 3 * cellSize - cellSize * 2);
        }

        // Paint pieces in queue
        if (piecesVisibleInQueue > 0) {
            int[] pieceQueue = gamePieceManager.getPieceQueue();
            for (int piece = 0; piece < piecesVisibleInQueue - 1; ++piece) {
                try {
                    // Defensive null/length check
                    if (piece >= pieceQueue.length) continue;
                    Integer shapeKey = pieceQueue[piece];
                    if (shapeKey == null) continue;

                    int[][] pieceArray = gamePieceManager.getShapeArray(gamePieceManager.SHAPE_MAP.get(shapeKey));
                    if (pieceArray == null) continue;

                    for (int column = 0; column < 4; ++column) {
                        for (int row = 0; row < 4; ++row) {
                            if (pieceArray[column][row] != 0) {
                                int xPieceOffset = xOffSet + (12 * cellSize) + row * cellSize;
                                int yPieceOffset = yOffSet + (3 * piece * cellSize) + column * cellSize + cellSize;

                                ImageIcon texture = TextureManager.getTexture(pieceArray[column][row]);
                                if (texture != null) {
                                    Image img = texture.getImage();
                                    G.drawImage(img, xPieceOffset, yPieceOffset, cellSize, cellSize, this);
                                } else {
                                    G.setColor(ColorMap.COLOR_MAP.get(pieceArray[column][row]));
                                    G.fillRect(xPieceOffset, yPieceOffset, cellSize, cellSize);
                                    G.setColor(Color.DARK_GRAY);
                                    G.drawRect(xPieceOffset, yPieceOffset, cellSize, cellSize);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Swallow exception to prevent crashes during queue update
                }
            }
        }
        // Paint hold box and pieces
        int xHoldBoxOffset = xOffSet - 7 * cellSize;
        G.setFont(new Font("Rockwell", Font.BOLD, cellSize));
        if (fullRainbow) {
            long elapsed = System.currentTimeMillis() - clearTypeTimestamp;
            int stage = (int) ((elapsed / 75) % rainbow.length);
            G.setColor(rainbow[stage]);
        } else {
            G.setColor(Color.WHITE);
        }
        G.drawString("Hold", xHoldBoxOffset + (int)(cellSize * 1.5), yOffSet - cellSize / 2);
        G.drawRect(xHoldBoxOffset, yOffSet, 6 * cellSize, 4 * cellSize);
        // Fill queue background
        G.setColor(new Color(0, 0, 0, 180)); // Slight transparent black
        G.fillRect(xHoldBoxOffset, yOffSet, 6 * cellSize, 4 * cellSize);
        if(!gamePlayfield.holdIsEmpty){
            int[][] piece = gamePlayfield.getCurrentHoldPiece();
            for (int column = 0; column < 4; ++column) {
                for (int row = 0; row < 4; ++row) {
                    if (piece[column][row] != 0) {
                        int xPieceOffset = xHoldBoxOffset + row * cellSize + cellSize;
                        int yPieceOffset = yOffSet + column * cellSize + cellSize;
                        ImageIcon texture = TextureManager.getTexture(piece[column][row]);
                        if (texture != null) {
                            Image img = texture.getImage();
                            G.drawImage(img, xPieceOffset, yPieceOffset, cellSize, cellSize, this);
                        } else {
                            G.setColor(ColorMap.COLOR_MAP.get(piece[column][row]));
                            G.fillRect(xPieceOffset, yPieceOffset, cellSize, cellSize);
                            G.setColor(Color.DARK_GRAY);
                            G.drawRect(xPieceOffset, yPieceOffset, cellSize, cellSize);
                        }
                    }
                }
            }
        }

        // Paint ghost pieces
        if (!gamePlayfield.noPiece) {
            Graphics2D ghostGraphics = (Graphics2D) G.create();
            ghostGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            boolean locking = (gamePlayfield.lockDelay > 0 && gamePlayfield.currentPieceLocationY == gamePlayfield.getLowestY());

            for (int index = 0; index < 4; ++index) {
                int lowest = gamePlayfield.getLowestY();
                int[][] pieceCoordinates = gamePlayfield.pieceCoordinates;
                int x = pieceCoordinates[index][0] + gamePlayfield.currentPieceLocationX;
                int y = pieceCoordinates[index][1] + lowest;

                if (locking) {
                    // Flicker transparency
                    long flickerPeriod = 600; // about 600ms full cycle
                    long phase = System.currentTimeMillis() % flickerPeriod;
                    float alpha = 0.3f + 0.2f * (float)Math.sin((2 * Math.PI * phase) / flickerPeriod); // flickers smoothly between 0.1–0.5
                    alpha = Math.min(0.5f, Math.max(0.1f, alpha));

                    ghostGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    ghostGraphics.setColor(Color.WHITE);
                    ghostGraphics.fillRect(xOffSet + x * cellSize, boardOffset + y * cellSize, cellSize, cellSize);
                }

                // Always draw crisp white outline
                ghostGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                ghostGraphics.setColor(Color.WHITE);
                ghostGraphics.drawRect(xOffSet + x * cellSize, boardOffset + y * cellSize, cellSize, cellSize);
            }
            ghostGraphics.dispose();
        }
        
        // Paint line clear text
        int combo = gamePlayfield.combo - 1;
        Font b2bFont = new Font("Rockwell", Font.ITALIC, cellSize - cellSize/3);
        int b2b = gamePlayfield.backToBack - 1;
        if (b2b > 0) {
            G.setFont(b2bFont);
            boolean flashRainbow = clearType.equals("Quad") && System.currentTimeMillis() - clearTypeTimestamp < CLEAR_TYPE_DISPLAY_MS;

            if (flashRainbow) {
                long elapsed = System.currentTimeMillis() - clearTypeTimestamp;
                int stage = (int) ((elapsed / 75) % rainbow.length);
                G.setColor(rainbow[stage]);
            } else {
                G.setColor(Color.YELLOW);
            }

            G.drawString("Back to back x " + b2b, xHoldBoxOffset + cellSize, yOffSet + 10 * cellSize);
        }

        if (!clearType.isEmpty() && System.currentTimeMillis() - clearTypeTimestamp < CLEAR_TYPE_DISPLAY_MS) {
            G.setFont(new Font("Rockwell", Font.ITALIC, cellSize));

            long elapsed = System.currentTimeMillis() - clearTypeTimestamp;

            // Determine color based on event
            if (clearType.equals("Quad") || perfectClear) {
                int stage = (int)((elapsed / 75) % rainbow.length);
                G.setColor(rainbow[stage]);
            } else if (!gamePlayfield.spinType.isEmpty()) { // It's a T-spin
                int stage = (int)((elapsed / 75) % purpleRainbow.length);
                G.setColor(purpleRainbow[stage]);
            } else {
                G.setColor(Color.YELLOW);
            }

            // Then draw your text like normal
            if (!gamePlayfield.spinType.isEmpty()) {
                G.drawString(gamePlayfield.spinType, xHoldBoxOffset + cellSize, yOffSet + (int)(5.5 * cellSize));
            }
            G.drawString(clearType, xHoldBoxOffset + cellSize, yOffSet + 7 * cellSize);

            if(combo > 0){
                G.setFont(new Font("Rockwell", Font.PLAIN, cellSize));
                if (clearType.equals("Quad")) {
                    elapsed = System.currentTimeMillis() - clearTypeTimestamp;
                    int stage = (int) ((elapsed / 75) % rainbow.length);
                    G.setColor(rainbow[stage]);
                } else {
                    G.setColor(Color.YELLOW);
                }
                G.drawString(combo + " Combo", xHoldBoxOffset + cellSize, yOffSet + (int)(11.5 * cellSize));
            }
            if (scoreFlashAmount > 0) {
                if (clearType.equals("Quad")) {
                    elapsed = System.currentTimeMillis() - clearTypeTimestamp;
                    int stage = (int) ((elapsed / 75) % rainbow.length);
                    G.setColor(rainbow[stage]);
                } else {
                    G.setColor(Color.YELLOW);
                }
                G.setFont(new Font("Rockwell", Font.ITALIC, cellSize));
                G.drawString("+" + scoreFlashAmount, xHoldBoxOffset + cellSize, yOffSet + (int)(8.5 * cellSize));
            }
            if (perfectClear) {
                // Rainbow effect
                elapsed = System.currentTimeMillis() - clearTypeTimestamp;
                int stage = (int)((elapsed / 75) % rainbow.length);
                G.setColor(rainbow[stage]);

                String pcText1 = "Perfect";
                String pcText2 = "Clear!";
                Font bigFont = new Font("Rockwell", Font.BOLD, (int)(cellSize * 2.5));
                G.setFont(bigFont);
                FontMetrics fm = G.getFontMetrics(bigFont);

                int textHeight = fm.getHeight();
                int pcWidth1 = fm.stringWidth(pcText1);
                int pcWidth2 = fm.stringWidth(pcText2);

                int centerX1 = xOffSet + (cellSize * 10 - pcWidth1) / 2;
                int centerX2 = xOffSet + (cellSize * 10 - pcWidth2) / 2;
                int baseY = yOffSet + (int)(8.5 * cellSize); // top line



                G.drawString(pcText1, centerX1, baseY);
                G.drawString(pcText2, centerX2, baseY + textHeight);
            }
        }

        int statsX = xHoldBoxOffset + (int)(cellSize * 1.5); // same as hold box
        int statsY = yOffSet + boardHeight - cellSize * 8;

        G.setFont(new Font("Rockwell", Font.BOLD, cellSize));
        if (fullRainbow) {
            long elapsed = System.currentTimeMillis() - clearTypeTimestamp;
            int stage = (int) ((elapsed / 75) % rainbow.length);
            G.setColor(rainbow[stage]);
        } else {
            G.setColor(Color.WHITE);
        }
        G.drawString("Lines: ", statsX, statsY +  3 * cellSize);
        G.drawString("" + gamePlayfield.totalLineClears, statsX, statsY +  3 * cellSize + cellSize);
        G.drawString("Level: ", statsX, statsY + 6 * cellSize);
        G.drawString("" + gamePlayfield.level, statsX, statsY + 6 * cellSize + cellSize);

        // Right side score (bottom-right)
        int scoreX = xOffSet + 12 * cellSize; // right of queue
        int scoreY = yOffSet + boardHeight - cellSize * 2;

        G.drawString("Score: ", scoreX, scoreY);
        G.drawString("" + gamePlayfield.score, scoreX, scoreY + cellSize);


    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(getParent().getWidth(), getParent().getHeight());
    }
}
