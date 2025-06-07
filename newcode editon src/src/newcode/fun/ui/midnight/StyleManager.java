package newcode.fun.ui.midnight;

import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StyleManager {
    public List<Style> styles = new ArrayList<>();
    private static Style currentStyle = null;

    public void init() {
        styles.addAll(Arrays.asList(
                new Style("1", HexColor.toColor("#283843"), HexColor.toColor("#a5baed")),
                new Style("2", HexColor.toColor("#0d3c4c"), HexColor.toColor("#15c9de")),
                new Style("3", HexColor.toColor("#405191"), HexColor.toColor("#6a83dd")),
                new Style("4", HexColor.toColor("#452f6d"), HexColor.toColor("#7444cd")),
                new Style("5", HexColor.toColor("#FF5858"), HexColor.toColor("#FFC8C8")),
                new Style("6", HexColor.toColor("#FF5858"), HexColor.toColor("#FFE8C1")),
                new Style("7", HexColor.toColor("#9992c6"), HexColor.toColor("#c9c1f8")),
                new Style("8", HexColor.toColor("#4d8a77"), HexColor.toColor("#4ee4b4")),
                new Style("9", HexColor.toColor("#7b2246"), HexColor.toColor("#cf125e")),
                new Style("10", HexColor.toColor("#484848"), HexColor.toColor("#ffffff")),
                new Style("11", HexColor.toColor("#5b0707"), HexColor.toColor("#bf5d5d")),
                new Style("12", HexColor.toColor("#333661"), HexColor.toColor("#434566")),
                new Style("13", HexColor.toColor("#ffffff"), HexColor.toColor("#ffffff")),
                new Style("14", HexColor.toColor("#cc5506"), HexColor.toColor("#e81b17"))
               )
        );
        currentStyle = styles.get(0);
    }

    public void setCurrentStyle(Style style) {
        currentStyle = style;
    }

    public static Style getCurrentStyle() {
        return currentStyle;
    }

    public static class HexColor {
        public static int toColor(String hexColor) {
            int argb = Integer.parseInt(hexColor.substring(1), 16);
            return reAlphaInt(argb, 255);
        }

        public static int reAlphaInt(final int color, final int alpha) {
            return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
        }
    }
}