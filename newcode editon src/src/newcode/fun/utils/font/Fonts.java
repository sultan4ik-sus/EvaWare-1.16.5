package newcode.fun.utils.font;

import lombok.SneakyThrows;
import newcode.fun.utils.font.common.Lang;
import newcode.fun.utils.font.styled.StyledFont;


public class Fonts {
    public static final String FONT_DIR = "/assets/minecraft/newcode/font/";

    public static volatile StyledFont[] gilroyBold = new StyledFont[24];
    public static volatile StyledFont[] msMedium = new StyledFont[24];
    public static volatile StyledFont[] msLight = new StyledFont[24];
    public static volatile StyledFont[] msRegular = new StyledFont[24];
    public static volatile StyledFont[] msSemiBold = new StyledFont[24];

    public static volatile StyledFont[] icon = new StyledFont[24];
    public static volatile StyledFont[] icon2 = new StyledFont[24];
    public static volatile StyledFont[] newcode = new StyledFont[24];
    public static volatile StyledFont[] blod = new StyledFont[24];

    @SneakyThrows
    public static void init() {
        for (int i = 8; i < 24;i++) {
            blod[i] = new StyledFont("nunito-bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            newcode[i] = new StyledFont("newcode.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            icon[i] = new StyledFont("icon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            icon2[i] = new StyledFont("icomoon.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 10; i < 23;i++) {
            gilroyBold[i] = new StyledFont("gilroy-bold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            msLight[i] = new StyledFont("Montserrat-Light.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            msMedium[i] = new StyledFont("Montserrat-Medium.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            msRegular[i] = new StyledFont("Montserrat-Regular.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
        for (int i = 8; i < 24;i++) {
            msSemiBold[i] = new StyledFont("Montserrat-SemiBold.ttf", i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }
    }
}