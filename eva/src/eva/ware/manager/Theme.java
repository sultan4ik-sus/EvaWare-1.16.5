package eva.ware.manager;

import com.google.common.eventbus.Subscribe;
import eva.ware.Evaware;
import eva.ware.events.EventUpdate;
import eva.ware.modules.impl.visual.Hud;
import eva.ware.utils.render.color.ColorUtility;

import java.util.HashMap;
import java.util.Map;

public class Theme {

    public static int textColor, darkTextColor, mainRectColor, darkMainRectColor, rectColor;

    private static final Map<String, int[]> THEME_COLORS = new HashMap<>();

    static {
        THEME_COLORS.put("Синий", new int[]{
                ColorUtility.rgb(124, 144, 222),
                ColorUtility.rgb(95, 112, 176),
                ColorUtility.rgb(33, 37, 54),
                ColorUtility.rgb(48, 57, 94),
                ColorUtility.rgb(69, 101, 181)
        });
        THEME_COLORS.put("Розовый", new int[]{
                ColorUtility.rgb(222, 124, 214),
                ColorUtility.rgb(169, 95, 176),
                ColorUtility.rgb(54, 33, 53),
                ColorUtility.rgb(94, 48, 86),
                ColorUtility.rgb(181, 69, 172)
        });
        THEME_COLORS.put("Фиолетовый", new int[]{
                ColorUtility.rgb(165, 124, 222),
                ColorUtility.rgb(129, 95, 176),
                ColorUtility.rgb(45, 33, 54),
                ColorUtility.rgb(76, 48, 94),
                ColorUtility.rgb(119, 69, 181)
        });
        THEME_COLORS.put("Эстетичный", new int[]{
                ColorUtility.rgb(124, 222, 209),
                ColorUtility.rgb(95, 176, 153),
                ColorUtility.rgb(33, 54, 49),
                ColorUtility.rgb(48, 94, 79),
                ColorUtility.rgb(69, 181, 146)
        });
        THEME_COLORS.put("Бирюзовый", new int[]{
                ColorUtility.rgb(64, 216, 224),
                ColorUtility.rgb(80, 208, 206),
                ColorUtility.rgb(28, 57, 59),
                ColorUtility.rgb(32, 102, 107),
                ColorUtility.rgb(90, 225, 230)
        });
        THEME_COLORS.put("Красный", new int[]{
                ColorUtility.rgb(222, 124, 124),
                ColorUtility.rgb(176, 95, 100),
                ColorUtility.rgb(54, 33, 33),
                ColorUtility.rgb(94, 48, 48),
                ColorUtility.rgb(181, 69, 69)
        });
        THEME_COLORS.put("Зеленый", new int[]{
                ColorUtility.rgb(124, 222, 137),
                ColorUtility.rgb(95, 176, 106),
                ColorUtility.rgb(33, 54, 35),
                ColorUtility.rgb(48, 94, 57),
                ColorUtility.rgb(69, 181, 101)
        });
        THEME_COLORS.put("Темный", new int[]{
                ColorUtility.rgb(255, 255, 255),
                ColorUtility.rgb(100, 100, 100),
                ColorUtility.rgb(20, 20, 20),
                ColorUtility.rgb(48, 48, 48),
                ColorUtility.rgb(61, 61, 61)
        });
    }

    public Theme() {
        updateTheme();
        Evaware.getInst().getEventBus().register(this);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        updateTheme();
    }

    public int getCustom() {
        return Hud.themeColor.getValue();
    }

    public void updateTheme() {
        String currentThemeMode = Hud.themeMode.getValue();
        String currentTheme = Hud.theme.getValue();

        if ("Кастом".equals(currentThemeMode)) {
            setCustomColors();
        } else if ("Шаблон".equals(currentThemeMode)) {
            setTemplateColors(currentTheme);
        }
    }

    private void setCustomColors() {
        int customColor = getCustom();
        float brpc = -0.3f;
        textColor = ColorUtility.multDark(customColor, 1.3f + brpc);
        darkTextColor = ColorUtility.multDark(customColor, 1.1f + brpc);
        mainRectColor = ColorUtility.multDark(customColor, 0.9f + brpc);
        darkMainRectColor = ColorUtility.multDark(customColor, 0.5f + brpc);
        rectColor = ColorUtility.multDark(customColor, 1f + brpc);
    }

    private void setTemplateColors(String theme) {
        int[] colors = THEME_COLORS.get(theme);
        if (colors != null) {
            setColors(colors);
        }
    }

    private void setColors(int[] colors) {
        textColor = colors[0];
        darkTextColor = colors[1];
        darkMainRectColor = colors[2];
        mainRectColor = colors[3];
        rectColor = colors[4];
    }
}
