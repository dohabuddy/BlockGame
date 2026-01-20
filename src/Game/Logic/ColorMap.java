/*
ColorMap.java maps the numbers in the shapes array to colors or pictures.
 */
package Game.Logic;

import java.awt.*;
import java.util.Map;

public class ColorMap {
    public static final Map<Integer, Color> COLOR_MAP = Map.of(
            1, Color.CYAN,  // I
            2, Color.MAGENTA,   // T
            3, Color.YELLOW,    // O
            4, Color.RED,       // Z
            5, Color.GREEN,     // S
            6, Color.BLUE,      // J
            7, Color.ORANGE     // L
    );
}
