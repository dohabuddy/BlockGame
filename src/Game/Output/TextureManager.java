package Game.Output;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final Map<Integer, ImageIcon> textures = new HashMap<>();

    public static void loadTextures() {
        for (int id = 1; id <= 7; id++) { // 1–7 for 7 Tetromino types
            String path = "/Resources/Textures/BLOCK_" + id + ".png";
            ImageIcon icon = new ImageIcon(TextureManager.class.getResource(path));
            textures.put(id, icon);
        }
    }

    public static ImageIcon getTexture(int id) {
        return textures.get(id);
    }
}