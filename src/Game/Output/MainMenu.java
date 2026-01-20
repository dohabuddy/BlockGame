package Game.Output;

import Game.Logic.GameManager;
import Game.Logic.GameState;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainMenu extends JPanel {
    public boolean fullscreen;

    private GameManager gameManager;
    private DisplayMaker displayMaker;
    private final Dimension defaultScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final JFrame mainMenuFrame;
    private JComboBox levelComboBox; // Wider scope to allow information to be accessible
    private SettingsMenu settingsMenu;
    private Image backgroundGifImage;


    public MainMenu(GameManager gameManager){
        this.gameManager = gameManager;
        this.displayMaker = new DisplayMaker(gameManager.soundPlayer);
        this.mainMenuFrame = makeMainMenu();
        this.settingsMenu = new SettingsMenu(gameManager, mainMenuFrame.getLayeredPane(), this.getFrameWidth(), this.getFrameHeight());
        openMainMenu();
        fullscreen = false;
        gameManager.soundPlayer.playSound(SoundEffects.GAME_OPEN,-25.0f);
    }
    public void openMainMenu(){
        switch(gameManager.getCurrentGameState()){
            case MAIN_MENU:
                gameManager.setCurrentGameState(GameState.MAIN_MENU);
                mainMenuFrame.setVisible(true);
                break;
            case PAUSED:
                gameManager.setCurrentGameState(GameState.MAIN_MENU);
                gameManager.endGame();
                mainMenuFrame.setVisible(true);
                break;
            default:
                System.out.println("INVALID ACTION - openMainMenu:MainMenu.java");
                break;
        }
    }
    public void closeMainMenu(){
        mainMenuFrame.dispose();
    }
    public int getFrameWidth(){
        return mainMenuFrame.getWidth();
    }
    public int getFrameHeight(){
        return mainMenuFrame.getHeight();
    }
    public int getStartLevel(){
        int levelIndex = levelComboBox.getSelectedIndex();
        return levelIndex + 1;
    }
    public void openSettingsMenu(){
        settingsMenu.showMenu();
    }

    private JFrame makeMainMenu(){
        // Set menu frame constants
        int frameHeight = defaultScreenSize.height/2; // Half the size of the screen
        int frameWidth = defaultScreenSize.width/2;
        // Set button constants
        int buttonWidth = (frameWidth/3);
        int buttonHeight = buttonWidth/2; // Make rectangle relative to frame size
        String gameFont = "Rockwell";
        int fontSize = 24;

        String buttonName; // Initialize variable "buttonName"
        // Set level array
        String[] levelList = new String[]{"Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6",
                "Level 7", "Level 8", "Level 9", "Level 10", "Level 11", "Level 12", "Level 13", "Level 14",
                "Level 15", "Level 16", "Level 17", "Level 18", "Level 19", "Level 20", "Level 21", "Level 22",
                "Level 23", "Level 24", "Level 25", "Level 26", "Level 27", "Level 28", "Level 29", "Level 30"};

        // Create main menu frame (JFrame)
        JFrame mainMenuFrame = displayMaker.frameMaker("Main Menu",
                DisplayMaker.closeOperationEnum.EXIT,
                frameWidth, frameHeight,
                true,
                DisplayMaker.layoutEnum.OTHER);

        // Create button panel to add buttons (JPanel)
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setSize(frameWidth,frameHeight); // Same size as frame
        GridBagConstraints buttonPanelGBC = new GridBagConstraints();
        buttonPanelGBC.insets = new Insets(10,0,10,0);

        // Create header (JLabel)
        JLabel header = new JLabel("Block Game", JLabel.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(frameHeight/10,0,10,0));
        header.setFont(new Font(gameFont, Font.BOLD, (frameWidth < frameHeight ? frameWidth/10 : frameHeight/10)));

        // Create play button (JButton)
        buttonName = "Play"; // Name used for button text and button name
        JButton playButton = displayMaker.buttonMaker(buttonName,
                buttonWidth, buttonHeight,
                gameFont, fontSize, true, false);
        playButton.setBackground(Color.WHITE);
        playButton.setOpaque(true);
        playButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 5),                  // outer border
                BorderFactory.createEmptyBorder(20, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));
        playButton.addActionListener(e -> gameManager.startGame()); // Add action listener to play button
        playButton.setName(buttonName); // Allow play button to be dynamically updated

        // Create level button (JComboBox)
        levelComboBox = displayMaker.comboBoxMaker(levelList,
                buttonWidth,buttonHeight,
                gameFont, fontSize,true,false);
        levelComboBox.setUI(new BasicComboBoxUI());
        levelComboBox.setFocusable(false);
        levelComboBox.setOpaque(true);
        levelComboBox.setBackground(Color.WHITE);
        levelComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 5),
                BorderFactory.createEmptyBorder(10, 10, 0, 0)
        ));
        levelComboBox.setName("levelComboBox");

        // Create settings button (JButton)
        buttonName = "Settings";
        JButton settingsButton = displayMaker.buttonMaker(buttonName,
                buttonWidth, buttonHeight,
                gameFont, fontSize,true, true);
        settingsButton.setFont(new Font("Rockwell", Font.BOLD | Font.ITALIC, fontSize));
        settingsButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 5),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));
        settingsButton.setBackground(Color.WHITE);
        settingsButton.setOpaque(true);
        // Add action listener to settings button: Open settings menu when left-clicked
        settingsButton.addActionListener(e -> gameManager.openSettingsMenu()); // Add action listener to play button
        settingsButton.setName(buttonName); // Allow settings button to be dynamically updated

        buttonPanelGBC.gridy = 0;
        buttonPanel.add(header, buttonPanelGBC);

        buttonPanelGBC.gridy = 1;
        buttonPanel.add(playButton, buttonPanelGBC);

        buttonPanelGBC.gridy = 2;
        buttonPanel.add(levelComboBox, buttonPanelGBC);

        buttonPanelGBC.gridy = 3;
        buttonPanel.add(settingsButton, buttonPanelGBC);
        mainMenuFrame.add(buttonPanel);

        // Update frame and button size when frame is resized
        mainMenuFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e){
                int newWidth = mainMenuFrame.getWidth();
                int newHeight = mainMenuFrame.getHeight();
                if(newWidth == JFrame.MAXIMIZED_HORIZ && newHeight == JFrame.MAXIMIZED_VERT){
                    fullscreen = true;
                }
                int newButtonLength = (newHeight < newWidth ? newHeight/3 : newWidth/3);
                Dimension newButtonSize = new Dimension(newButtonLength, newButtonLength/2);
                header.setBorder(BorderFactory.createEmptyBorder(newHeight/10,0,0,0));
                header.setFont(new Font(gameFont, Font.BOLD, (newWidth < newHeight ? newWidth/10 : newHeight/10)));
                playButton.setPreferredSize(newButtonSize);
                playButton.setFont(new Font(gameFont, Font.BOLD,newButtonLength/playButton.getName().length()));
                levelComboBox.setPreferredSize(newButtonSize);
                levelComboBox.setFont(new Font(gameFont, Font.BOLD,newButtonLength/levelComboBox.getName().length()));
                settingsButton.setPreferredSize(newButtonSize);
                settingsButton.setFont(new Font(gameFont, Font.BOLD | Font.ITALIC,newButtonLength/settingsButton.getName().length()));
                if(settingsMenu.menuIsVisible()){
                    settingsMenu.resizeSettingsMenu();
                }
                mainMenuFrame.revalidate();
                mainMenuFrame.repaint();
            }
        });
        return mainMenuFrame;
    }
}
