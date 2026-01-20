package Game.Output;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;

public class DisplayMaker {
    private final SoundPlayer soundPlayer;

    public DisplayMaker(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }
    public enum layoutEnum{
        BAG,
        BORDER,
        CARD,
        FLOW,
        GRID,
        OTHER
    }
    public enum closeOperationEnum{
        HIDE,
        NOTHING,
        DISPOSE,
        EXIT
    }
    public JFrame frameMaker(String frameTitle, DisplayMaker.closeOperationEnum closeOperation, int width, int height, boolean centered, DisplayMaker.layoutEnum layout){
        JFrame frame = new JFrame(frameTitle);
        switch(closeOperation) {
            case HIDE:
                frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE); // Hide window, window is still displayable, program continues
                break;
            case NOTHING:
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Allows custom action to be taken in user defined method, program continues
                break;
            case DISPOSE:
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // Hide and dispose window, program continues
                break;
            case EXIT:
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Dispose of window and end program
                break;
            default:
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                break;
        }
        frame.setSize(width, height); // Set size of frame, pixels width x height
        if(centered){
            frame.setLocationRelativeTo(null); // Center Window
        }
        switch (layout) { // Set layout of panels and buttons
            case BAG:
                frame.setLayout(new GridBagLayout()); // Customizable grid layout
                break;
            case BORDER:
                frame.setLayout(new BorderLayout()); // North, South, East, West, and center
                break;
            case CARD:
                frame.setLayout(new CardLayout()); // Overlaying cards
                break;
            case FLOW:
                frame.setLayout(new FlowLayout()); // Rows according to width, number, and size of panel and components
                break;
            case GRID:
                frame.setLayout(new GridLayout()); // Set grid for panels and buttons to snap to
                break;
            default:
                break;
        }
        return frame;
    }
    public JPanel panelMaker(layoutEnum layout, int width, int height) {
        JPanel panel = new JPanel();
        switch (layout) { // Set layout of panels and buttons
            case BAG:
                panel.setLayout(new GridBagLayout()); // Customizable grid layout
                break;
            case BORDER:
                panel.setLayout(new BorderLayout()); // North, South, East, West, and center
                break;
            case CARD:
                panel.setLayout(new CardLayout()); // Overlaying cards
                break;
            case FLOW:
                panel.setLayout(new FlowLayout()); // Rows according to width, number, and size of panel and components
                break;
            case GRID:
                panel.setLayout(new GridLayout()); // Set grid for panels and buttons to snap to
                break;
            default:
                break;
        }
        panel.setPreferredSize(new Dimension(width,height));
        return panel;
    }
    public JButton buttonMaker(String buttonTitle, int width, int height, String font, int fontSize, boolean bold, boolean italic){
        JButton button = new JButton(buttonTitle);
        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (bold && italic) button.setFont(new Font(font, Font.BOLD | Font.ITALIC, fontSize));
        else if (bold) button.setFont(new Font(font, Font.BOLD, fontSize));
        else if (italic) button.setFont(new Font(font, Font.BOLD, fontSize));

        button.addActionListener(e -> soundPlayer.playSound(SoundEffects.CLICK, -35.0f));

        return button;
    }
    public JComboBox comboBoxMaker(Object[] arrayList, int width, int height, String font, int fontSize, boolean bold, boolean italic){
        int objectLength = arrayList[0].toString().length();
        JComboBox comboBox = new JComboBox(arrayList);
        comboBox.setPreferredSize(new Dimension(width,height));
        if(bold && italic) {
            comboBox.setFont(new Font(font, Font.BOLD | Font.ITALIC, fontSize));
        } else if(bold){
            comboBox.setFont(new Font(font, Font.BOLD, fontSize));
        } else if(italic){
            comboBox.setFont(new Font(font, Font.ITALIC, fontSize));
        } else {
            comboBox.setFont(new Font(font, Font.PLAIN, fontSize));
        }
        return comboBox;
    }
    public JLabel labelMaker(String text, int width, int height, String font, int fontSize, boolean bold, boolean italic) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(width,height));
        if (bold && italic) {
            label.setFont(new Font(font, Font.BOLD | Font.ITALIC, fontSize));
        } else if (bold) {
            label.setFont(new Font(font, Font.BOLD, fontSize));
        } else if (italic) {
            label.setFont(new Font(font, Font.ITALIC, fontSize));
        } else {
            label.setFont(new Font(font, Font.PLAIN, fontSize));
        }
        return label;
    }
    public JDialog dialogMaker(JFrame frameOwner, String dialogTitle, String font){
        int fontSize = 14;
        JDialog dialog = new JDialog(frameOwner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setFont(new Font(font, Font.PLAIN, fontSize));
        dialog.setLayout(new FlowLayout());
        dialog.setSize(new Dimension(200,200));
        dialog.isModal();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frameOwner);
        return dialog;
    }
}

