package Game.Output;

import Game.Logic.GameManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class GameOver {
    GameManager gameManager;
    JLayeredPane layeredPane;
    DisplayMaker displayMaker;
    private int frameWidth;
    private int frameHeight;
    private JPanel gameOverPanel;
    private JPanel backgroundPanel;
    JLabel title;
    JLabel score;
    JButton playAgainButton;
    JButton settingsButton;
    JButton mainMenuButton;
    JButton closeButton;
    public GameOver(GameManager gameManager, JLayeredPane layeredPane, int width, int height){
        this.displayMaker = new DisplayMaker(gameManager.soundPlayer);
        this.gameManager = gameManager;
        this.layeredPane = layeredPane;
        this.frameWidth = width;
        this.frameHeight = height;
        makeGameOverPanel(layeredPane, width, height);
        setMenuVisible();
        resizePanels(width,height);
        System.out.println("CREATING NEW GAME OVER PANEL");
    }
    public void setMenuVisible(){
        backgroundPanel.setVisible(true);
        gameOverPanel.setVisible(true);
        System.out.println("SETTING GAME OVER MENU VISIBLE");
    }
    public void hideMenu(){
        backgroundPanel.setVisible(false);
        gameOverPanel.setVisible(false);
    }
    public void openSettings(){
        hideMenu();
        gameManager.openSettingsMenu();
    }

    public void resizePanels(int newWidth, int newHeight){
        frameWidth = newWidth;
        frameHeight = newHeight;
        int panelWidth = frameWidth / 2;
        int panelHeight = newHeight - newHeight / 4;
        int newFontSize = panelWidth/10;
        int scoreFontSize = newFontSize - newFontSize/3;
        int buttonFontSize = newFontSize/3;
        int buttonWidth = frameWidth / 6;
        int buttonHeight = buttonWidth / 3;
        Dimension newButtonSize = new Dimension(buttonWidth,buttonHeight);

        backgroundPanel.setBounds(0, 0, frameWidth, frameHeight);
        gameOverPanel.setBounds((frameWidth - panelWidth) / 2, (frameHeight - panelHeight) / 2, panelWidth, panelHeight);
        title.setFont(new Font("Rockwell", Font.BOLD, newFontSize));
        title.setBorder(BorderFactory.createEmptyBorder(panelHeight / 10, 0, 10, 0));

        score.setFont(new Font("Rockwell", Font.PLAIN, scoreFontSize));
        score.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        playAgainButton.setFont(new Font("Rockwell", Font.PLAIN, buttonFontSize));
        settingsButton.setFont(new Font("Rockwell", Font.PLAIN, buttonFontSize));
        mainMenuButton.setFont(new Font("Rockwell", Font.PLAIN, buttonFontSize));
        closeButton.setFont(new Font("Rockwell", Font.PLAIN, buttonFontSize));

        playAgainButton.setPreferredSize(newButtonSize);
        playAgainButton.setMaximumSize(newButtonSize);
        settingsButton.setPreferredSize(newButtonSize);
        settingsButton.setMaximumSize(newButtonSize);
        mainMenuButton.setPreferredSize(newButtonSize);
        mainMenuButton.setMaximumSize(newButtonSize);
        closeButton.setPreferredSize(newButtonSize);
        closeButton.setMaximumSize(newButtonSize);

        backgroundPanel.repaint();
        gameOverPanel.repaint();
    }
    private void makeDimBackground(JLayeredPane layeredPane, int width, int height) {
        backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent white
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        backgroundPanel.setBounds(0, 0, width, height);
        backgroundPanel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent white
        backgroundPanel.setOpaque(false);
        backgroundPanel.setBounds(0, 0, width, height);
        backgroundPanel.addMouseListener(new MouseAdapter() {});
        layeredPane.add(backgroundPanel, JLayeredPane.MODAL_LAYER);
        backgroundPanel.setVisible(false);
    }
    private void makeGameOverPanel(JLayeredPane layeredPane, int width, int height){
        int panelWidth = width / 2;
        int panelHeight = height - height / 4;

        gameOverPanel = displayMaker.panelMaker(DisplayMaker.layoutEnum.FLOW, width, height);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setOpaque(true);
        gameOverPanel.setBounds((width - panelWidth) / 2, (height - panelHeight) / 2, panelWidth, panelHeight);
        gameOverPanel.setBackground(Color.WHITE);
        gameOverPanel.setFocusable(true);
        gameOverPanel.requestFocus();

        int titleFontSize = panelWidth/10;
        int scoreFontSize = titleFontSize - titleFontSize/3;

        title = new JLabel("Game Over!");
        title.setFont(new Font("Rockwell", Font.BOLD, titleFontSize));
        title.setBorder(BorderFactory.createEmptyBorder(panelHeight / 10, 0, 10, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        score = new JLabel("Final Score: " + gameManager.gamePlayField.score);
        score.setFont(new Font("Rockwell", Font.PLAIN, scoreFontSize));
        score.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        score.setAlignmentX(Component.CENTER_ALIGNMENT);

        int buttonWidth = width / 5;
        int buttonHeight = buttonWidth / 3;
        String font = "Rockwell";
        int buttonFontSize = titleFontSize/2 - titleFontSize/4;

        playAgainButton = displayMaker.buttonMaker("Play Again", buttonWidth, buttonHeight, font, buttonFontSize, true, false);
        playAgainButton.addActionListener(e -> gameManager.restartGame());
        playAgainButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));


        settingsButton = displayMaker.buttonMaker("Settings", buttonWidth, buttonHeight, font, buttonFontSize, true, false);
        settingsButton.addActionListener(e -> openSettings());
        settingsButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));


        mainMenuButton = displayMaker.buttonMaker("Main Menu", buttonWidth, buttonHeight, font, buttonFontSize, true, false);
        mainMenuButton.addActionListener(e -> gameManager.openMainMenu());
        mainMenuButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));


        closeButton = displayMaker.buttonMaker("Close Game", buttonWidth, buttonHeight, font, buttonFontSize, true, false);
        closeButton.addActionListener(e -> gameManager.quit());
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 0, 0, 0)                               // inner padding: top, left, bottom, right
        ));


        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        gameOverPanel.add(title);
        gameOverPanel.add(Box.createVerticalStrut(8));
        gameOverPanel.add(score);
        gameOverPanel.add(Box.createVerticalStrut(8));
        gameOverPanel.add(playAgainButton);
        gameOverPanel.add(Box.createVerticalStrut(8));
        gameOverPanel.add(settingsButton);
        gameOverPanel.add(Box.createVerticalStrut(8));
        gameOverPanel.add(mainMenuButton);
        gameOverPanel.add(Box.createVerticalStrut(8));
        gameOverPanel.add(closeButton);
        gameOverPanel.add(Box.createVerticalStrut(8));


        gameOverPanel.revalidate();

        layeredPane.add(gameOverPanel, JLayeredPane.POPUP_LAYER);
        layeredPane.repaint();
        gameOverPanel.setVisible(false);
        makeDimBackground(layeredPane, width, height);
    }
}
