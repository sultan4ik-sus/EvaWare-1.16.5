package newcode.fun.ui.midnight;

import newcode.fun.utils.render.ColorUtils;

public class Style {
    public String name;
    public int[] colors;

    public Style(String name, int... colors) {
        this.name = name;
        this.colors = colors;
    }

    public int getColor(int index) {
        if ("13".equals(name)) {
            return ColorUtils.astolfo(10, index, 0.5F, 1.0F, 1.0F);
        }
        return ColorUtils.gradient(5, index, colors);
    }

    public int getFirstColor() {
        return colors.length > 0 ? colors[0] : 0;
    }

    public int getSecondaryColor() {
        return colors.length > 1 ? colors[1] : getFirstColor();
    }

    public int getFerstyColor() {
        if (colors.length > 1) {
            return colors[0];
        }
        return colors[0];
    }
}
