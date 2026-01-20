package Game.Output;

import Game.Logic.GameManager;

import javax.swing.*;
import java.awt.*;

public class PauseMenu {
    private DisplayMaker displayMaker;
    private GameManager gameManager;
    private SettingsMenu settingsMenu;
    private JPanel pausePanel;
    private JLabel title;
    private JButton resumeButton;
    private JButton settingsMenuButton;
    private JButton restartButton;
    private JButton mainMenuButton;
    private JButton quitGameButton;

    private int frameWidth;
    private int frameHeight;
    public boolean settingsMenuIsOpen;

    public PauseMenu(GameManager gameManager, JLayeredPane layeredPane, int width, int height, SettingsMenu settingsMenu){
        this.displayMaker = new DisplayMaker(gameManager.soundPlayer);
        this.gameManager = gameManager;
        this.frameWidth = width;
        this.frameHeight = height;
        this.settingsMenu = settingsMenu;
        makePauseMenu(layeredPane, width, height);
        settingsMenuIsOpen = false;
    }

    public void showMenu(){
        pausePanel.setVisible(true);
    }
    public void showSettingsMenu(){
        settingsMenuIsOpen = true;
        settingsMenu.showMenu();
    }
    public void closePauseMenu(){
        if(settingsMenuIsOpen){
            settingsMenu.closeSettingsMenu();
            settingsMenuIsOpen = false;
        }
        pausePanel.setVisible(false);
    }
    public void resizePauseMenu(int newWidth, int newHeight){
        pausePanel.setBounds(0,0,newWidth,newHeight);
        pausePanel.setPreferredSize(new Dimension(newWidth,newHeight));
        int titleFontSize = newWidth/20;
        int buttonFont = titleFontSize/3;
        int buttonSize = newWidth/5;
        Dimension buttonDimension = new Dimension(buttonSize,buttonSize/3);
        title.setBorder(BorderFactory.createEmptyBorder(newHeight / 10, 0, 10, 0));
        title.setFont(new Font("Rockwell", Font.BOLD, titleFontSize));
        resumeButton.setFont(new Font("Rockwell", Font.BOLD, buttonFont));
        resumeButton.setPreferredSize(buttonDimension);
        resumeButton.setMaximumSize(buttonDimension);
        settingsMenuButton.setFont(new Font("Rockwell", Font.BOLD, buttonFont));
        settingsMenuButton.setPreferredSize(buttonDimension);
        settingsMenuButton.setMaximumSize(buttonDimension);
        restartButton.setFont(new Font("Rockwell", Font.BOLD, buttonFont));
        restartButton.setPreferredSize(buttonDimension);
        restartButton.setMaximumSize(buttonDimension);
        mainMenuButton.setFont(new Font("Rockwell", Font.BOLD, buttonFont));
        mainMenuButton.setPreferredSize(buttonDimension);
        mainMenuButton.setMaximumSize(buttonDimension);
        quitGameButton.setFont(new Font("Rockwell", Font.BOLD, buttonFont));
        quitGameButton.setPreferredSize(buttonDimension);
        quitGameButton.setMaximumSize(buttonDimension);
        pausePanel.revalidate();
        pausePanel.repaint();
    }

    public void makePauseMenu(JLayeredPane layeredPane, int width, int height){
        pausePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 100)); // Semi-transparent white
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        pausePanel.setOpaque(false);
        pausePanel.setLayout(new BoxLayout(pausePanel,BoxLayout.Y_AXIS));
        pausePanel.setBounds(0,0,width,height);
        Color backgroundColor = new Color(0,0,0,100);
        pausePanel.setBackground(backgroundColor);

        int titleFontSize = width/20;

        title = new JLabel("Game Paused");
        title.setFont(new Font("Rockwell", Font.BOLD, titleFontSize));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(height / 10, 0, 10, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        int buttonSize = width/5;
        String font = "Rockwell";
        int fontSize = titleFontSize/3;

        String name = "Resume Game";
        resumeButton = displayMaker.buttonMaker(name, buttonSize, buttonSize/3, font, fontSize,true, false);
        resumeButton.setName(name);
        resumeButton.addActionListener(e -> gameManager.resumeGame());
        resumeButton.setFocusable(false);
        resumeButton.setBackground(Color.WHITE);
        resumeButton.setOpaque(true);
        resumeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));

        name = "Settings Menu";
        settingsMenuButton = displayMaker.buttonMaker(name, buttonSize, buttonSize/3, font, fontSize, true, false);
        settingsMenuButton.setName(name);
        settingsMenuButton.addActionListener(e -> gameManager.openSettingsMenu());
        settingsMenuButton.setFocusable(false);
        settingsMenuButton.setBackground(Color.WHITE);
        settingsMenuButton.setOpaque(true);
        settingsMenuButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));

        name = "Restart Game";
        restartButton = displayMaker.buttonMaker(name, buttonSize, buttonSize/3, font, fontSize, true, false);
        restartButton.setName(name);
        restartButton.addActionListener(e -> gameManager.pauseRestart());
        restartButton.setFocusable(false);
        restartButton.setBackground(Color.WHITE);
        restartButton.setOpaque(true);
        restartButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));

        name = "Main Menu";
        mainMenuButton = displayMaker.buttonMaker(name, buttonSize, buttonSize/3, font, fontSize, true, false);
        mainMenuButton.setName(name);
        mainMenuButton.addActionListener(e -> gameManager.backToMainMenu());
        mainMenuButton.setFocusable(false);
        mainMenuButton.setBackground(Color.WHITE);
        mainMenuButton.setOpaque(true);
        mainMenuButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));

        name = "Quit Game";
        quitGameButton = displayMaker.buttonMaker(name, buttonSize, buttonSize/3, font, fontSize, true, false);
        quitGameButton.setName(name);
        quitGameButton.addActionListener(e -> gameManager.quit());
        quitGameButton.setFocusable(false);
        quitGameButton.setBackground(Color.WHITE);
        quitGameButton.setOpaque(true);
        quitGameButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));

        pausePanel.add(title);
        pausePanel.add(Box.createVerticalStrut(8));
        pausePanel.add(resumeButton);
        pausePanel.add(Box.createVerticalStrut(8));
        pausePanel.add(settingsMenuButton);
        pausePanel.add(Box.createVerticalStrut(8));
        pausePanel.add(restartButton);
        pausePanel.add(Box.createVerticalStrut(8));
        pausePanel.add(mainMenuButton);
        pausePanel.add(Box.createVerticalStrut(8));
        pausePanel.add(quitGameButton);
        pausePanel.add(Box.createVerticalStrut(8));
        layeredPane.add(pausePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.revalidate();
        pausePanel.revalidate();
        pausePanel.setVisible(false);
    }

}
