package Game.Output;

import Game.Input.Controls;
import Game.Input.PlayerInput;
import Game.Logic.GameManager;
import Game.Logic.GameState;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class SettingsMenu {
    private GameManager gameManager;
    private JPanel masterPanel;
    private JPanel settingsPanel;
    private JScrollPane scrollPane;
    private PlayerInput playerInput;
    private final Map<Controls, JButton> controlButtons = new HashMap<>();
    private JComboBox queueSettingComboBox;
    private DisplayMaker displayMaker;
    private JSlider dasSlider;
    private JSlider arrSlider;
    private JSlider sdfSlider;
    public JSlider musicVolumeSlider;
    public JSlider soundVolumeSlider;
    int frameWidth;
    int frameHeight;

    public SettingsMenu(GameManager gameManager, JLayeredPane layeredPane, int width, int height) {
        this.gameManager = gameManager;
        this.displayMaker = new DisplayMaker(gameManager.soundPlayer);
        this.frameWidth = width;
        this.frameHeight = height;
        makeSettingsMenu(layeredPane, width, height);
    }
    public void closeSettingsMenu(){
        masterPanel.setVisible(false);
        gameManager.updatePlayerInput();
        if(gameManager.currentGameState == GameState.GAME_OVER){
            gameManager.showGameOver();
        }
    }
    public int getSDF(){
        return sdfSlider.getValue();
    }
    private void changeBind(Controls control, JButton buttonToUpdate) {
        JDialog dialog = new JDialog((JFrame) null, "Rebind Key", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JLabel message = new JLabel("Press a new key for: " + control.name(), SwingConstants.CENTER);
        message.setFont(new Font("Rockwell", Font.PLAIN, 16));
        dialog.add(message, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton, BorderLayout.SOUTH);

        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                control.setBind(keyCode);
                buttonToUpdate.setText(Controls.getReadableKeyName(keyCode)); // Update the visible button
                dialog.dispose();
            }
        });

        dialog.setFocusable(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
    private void resetSettings(){

        for (Controls control : Controls.values()) {
            int key = control.getDefaultKeyBind();
            control.setBind(key);
            controlButtons.get(control).setText(Controls.getReadableKeyName(key));
        }
        queueSettingComboBox.setSelectedIndex(5); // or whatever your default is

        gameManager.setDAS(150);
        gameManager.setARR(50);
        gameManager.setSDF(1);

        dasSlider.setValue(150);
        arrSlider.setValue(50);
        sdfSlider.setValue(1);

        musicVolumeSlider.setValue(100);
        soundVolumeSlider.setValue(100);
    }
    public void makeSettingsMenu(JLayeredPane layeredPane, int width, int height){

        masterPanel = new JPanel(new BorderLayout());

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Color.WHITE);

        int buttonSize = width / 10;
        String font = "Rockwell";
        int fontSize = 24;

        // Top Bar Setup
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setMaximumSize(new Dimension(frameWidth, (frameHeight / 10) + 10)); // Slightly taller
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 3),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));
        String name = "Back";
        JButton backButton = displayMaker.buttonMaker(name, buttonSize + 10, buttonSize / 2 + 5, font, fontSize + 2, true, false);
        backButton.setName(name);
        backButton.addActionListener(e -> closeSettingsMenu());

        JLabel settingsText = new JLabel("Settings Menu",JLabel.CENTER);
        settingsText.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        settingsText.setFont(new Font(font, Font.BOLD, fontSize * 5 / 3));

        name = "Reset All";
        JButton resetButton = displayMaker.buttonMaker("Reset", buttonSize + 10, buttonSize / 2 + 5, font, fontSize + 2, true, false);
        resetButton.setName("Reset");
        resetButton.addActionListener(e -> resetSettings());

        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(backButton);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(settingsText);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(resetButton);
        topBar.add(Box.createHorizontalStrut(10));

        // Controls Panel
        JLabel controlText = new JLabel("Controls");
        controlText.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        controlText.setFont(new Font(font, Font.BOLD, 20));
        controlText.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the label
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 2),                  // outer border
                BorderFactory.createEmptyBorder(10, 50, 10, 50)                               // inner padding: top, left, bottom, right
        ));
        controlsPanel.add(controlText);

        int controlFontSize = fontSize * 3 / 4;
        controlsPanel.add(makeControlEntry("Pause", Controls.PAUSE, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Move Left", Controls.MOVE_LEFT, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Move Right", Controls.MOVE_RIGHT, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Rotate Clockwise", Controls.ROTATE_CLOCKWISE, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Rotate Counter Clockwise", Controls.ROTATE_COUNTER_CLOCKWISE, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Rotate 180", Controls.ROTATE_180, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Soft Drop", Controls.SOFT_DROP, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Hard Drop", Controls.HARD_DROP, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Hold", Controls.HOLD, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(makeControlEntry("Restart", Controls.RESTART, buttonSize, font, controlFontSize));
        controlsPanel.add(Box.createVerticalStrut(8));

        // Misc Settings Panel
        JLabel handlingLabel = new JLabel("Miscellaneous");
        handlingLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        handlingLabel.setFont(new Font(font, Font.BOLD, 20));
        handlingLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the label
        JPanel handlingPanel = new JPanel();
        handlingPanel.setLayout(new BoxLayout(handlingPanel, BoxLayout.Y_AXIS));
        handlingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 2),                  // outer border
                BorderFactory.createEmptyBorder(10, 50, 10, 50)                               // inner padding: top, left, bottom, right
        ));
        handlingPanel.add(handlingLabel);
        // Queue ComboBox
        name = "Pieces Visible in Queue";
        JLabel queueSetting = displayMaker.labelMaker(name, frameWidth / 2, frameWidth / 4, font, controlFontSize, true, true);
        Integer[] queueSettingList = new Integer[]{0, 1, 2, 3, 4, 5};
        queueSettingComboBox = displayMaker.comboBoxMaker(queueSettingList, buttonSize, buttonSize / 2, font, controlFontSize, false, true);
        queueSettingComboBox.setSelectedIndex(5);
        queueSettingComboBox.setUI(new BasicComboBoxUI());
        queueSettingComboBox.setFocusable(false);
        queueSettingComboBox.setOpaque(true);
        queueSettingComboBox.setBackground(Color.WHITE);
        queueSettingComboBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        JPanel queueSettingPanel = new JPanel(new GridLayout(1, 2));
        queueSettingPanel.add(queueSetting);
        queueSettingPanel.add(queueSettingComboBox);
        queueSettingPanel.setPreferredSize(new Dimension(frameWidth, frameHeight / 12));
        queueSettingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(5, 10, 0, 1)                               // inner padding: top, left, bottom, right
        ));
        // Sliders
        dasSlider = makeSlider(50, 300, gameManager.DAS, e -> gameManager.setDAS(((JSlider) e.getSource()).getValue()));
        dasSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));
        arrSlider = makeSlider(20, 70, gameManager.ARR, e -> gameManager.setARR(((JSlider) e.getSource()).getValue()));
        arrSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));
        sdfSlider = makeSlider(1, 20, gameManager.SDF, e -> gameManager.setSDF(((JSlider) e.getSource()).getValue()));
        sdfSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));

        handlingPanel.add(queueSettingPanel);
        handlingPanel.add(Box.createVerticalStrut(8));
        handlingPanel.add(makeSliderSetting("DAS (Delayed Auto Shift)", dasSlider));
        handlingPanel.add(Box.createVerticalStrut(8));
        handlingPanel.add(makeSliderSetting("ARR (Auto Repeat Delay)", arrSlider));
        handlingPanel.add(Box.createVerticalStrut(8));
        handlingPanel.add(makeSliderSetting("SDF (Soft Drop Factor)", sdfSlider));
        handlingPanel.add(Box.createVerticalStrut(8));


        // Sound Panel
        JPanel soundPanel = new JPanel();
        soundPanel.setLayout(new BoxLayout(soundPanel, BoxLayout.Y_AXIS));
        soundPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 2),                  // outer border
                BorderFactory.createEmptyBorder(10, 50, 10, 50)                               // inner padding: top, left, bottom, right
        ));
        musicVolumeSlider = makeSlider(0, 100, gameManager.musicPercent, e -> {
            int value = ((JSlider) e.getSource()).getValue();
            float db = (value / 100f) * (-5f - (-80f)) + (-80f);
            gameManager.setMusicVolume(value);
            gameManager.musicPlayer.setMasterVolume(db);
        });
        musicVolumeSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));
        soundVolumeSlider = makeSlider(0, 100, gameManager.soundEffectsPercent, e -> {
            int value = ((JSlider) e.getSource()).getValue();
            float db = (value / 100f) * (0f - (-80f)) + (-80f);
            gameManager.setSoundVolume(value);
            gameManager.soundPlayer.setVolume(db);
        });
        soundVolumeSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1),                  // outer border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                               // inner padding: top, left, bottom, right
        ));
        soundPanel.add(makeSliderSetting("Music Volume", musicVolumeSlider));
        soundPanel.add(Box.createVerticalStrut(10));
        soundPanel.add(makeSliderSetting("Sound Effects Volume", soundVolumeSlider));
        soundPanel.add(Box.createVerticalStrut(10));


        // Combine Panels
        settingsPanel.add(controlsPanel);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(handlingPanel);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(soundPanel);

        JPanel centeringPanel = new JPanel();
        centeringPanel.setLayout(new BoxLayout(centeringPanel, BoxLayout.X_AXIS));
        centeringPanel.setOpaque(true);
        centeringPanel.setBackground(Color.WHITE);
        centeringPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 50, 25));
        centeringPanel.add(Box.createHorizontalGlue());
        centeringPanel.add(settingsPanel);
        centeringPanel.add(Box.createHorizontalGlue());

        // Add padding around the panel
        int settingsWidth = (int) (width * 0.85);

        settingsPanel.setPreferredSize(new Dimension(settingsWidth, settingsPanel.getPreferredSize().height));
        scrollPane = new JScrollPane(centeringPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        masterPanel.add(topBar, BorderLayout.NORTH);
        masterPanel.add(wrapperPanel, BorderLayout.CENTER);

        layeredPane.add(masterPanel, JLayeredPane.POPUP_LAYER);
        masterPanel.setVisible(false);
        scrollPane.setVisible(false);
    }

    private JPanel makeControlEntry(String label, Controls control, int buttonSize, String font, int fontSize) {
        JLabel keyLabel = displayMaker.labelMaker(label, frameWidth / 2, frameWidth / 4, font, fontSize, true, true);
        keyLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        keyLabel.setName(label + " Keybind");
        JButton keyButton = displayMaker.buttonMaker(Controls.getReadableKeyName(control.getCurrentBind()), buttonSize, buttonSize, font, fontSize, false, true);
        keyButton.setName(label);
        keyButton.addActionListener(e -> changeBind(control, keyButton));
        controlButtons.put(control, keyButton);
        JPanel panel = makeKeybindSetting(label, keyLabel, keyButton);
        panel.setFocusable(true);
        return panel;
    }

    private JPanel makeKeybindSetting(String title, JLabel label, JButton button) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(label);
        panel.add(button);
        panel.setPreferredSize(new Dimension(frameWidth, frameHeight / 12));
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1));
        return panel;
    }


    public void showMenu() {
        System.out.println("SHOW SETTINGS - showMenu():SettingsMenu.java");
        masterPanel.setVisible(true);
        scrollPane.setVisible(true);
        masterPanel.revalidate();
        scrollPane.revalidate();
        masterPanel.repaint();
        scrollPane.repaint();
        resizeSettingsMenu();
    }
    public boolean menuIsVisible(){
        return masterPanel.isVisible();
    }
    public void resizeSettingsMenu() {
        // Update stored dimensions
        this.frameWidth = masterPanel.getParent().getWidth();
        this.frameHeight = masterPanel.getParent().getHeight();

        // Set bounds of master panel
        masterPanel.setBounds(0, 0, frameWidth, frameHeight);

        // Calculate height available for scroll pane (excluding top bar height)
        int topBarHeight = frameHeight / 15; // Same logic as in topBar
        int scrollHeight = frameHeight - topBarHeight;

        // Resize scroll pane to fit within available height
        scrollPane.setPreferredSize(new Dimension(frameWidth, scrollHeight));


        // Let the inner settingsPanel (inside buffer) resize naturally
        int contentWidth = (int)(frameWidth * 0.75); // Slightly narrower than scroll area
        settingsPanel.setPreferredSize(new Dimension(contentWidth, settingsPanel.getPreferredSize().height));
        // Ensure layout updates happen
        masterPanel.revalidate();
        scrollPane.revalidate();
        masterPanel.repaint();
        scrollPane.repaint();
        System.out.println("RESIZING - resizeSettingsMenu():SettingsMenu.java");
    }
    public int getNumberOfPiecesVisibleInQueue(){
        return queueSettingComboBox.getSelectedIndex() + 1;
    }
    private JSlider makeSlider( int min, int max, int initial, ChangeListener listener) {
        JSlider slider = new JSlider(min, max, initial);
        slider.addChangeListener(listener);
        slider.setMajorTickSpacing((max - min) / 10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setFont(new Font("Rockwell", Font.ITALIC, 14));
        return slider;
    }
    private JPanel makeSliderSetting(String labelText, JSlider slider){
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Rockwell", Font.BOLD | Font.ITALIC, 16));
        label.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 0));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

}
