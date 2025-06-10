package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import eva.ware.Evaware;
import eva.ware.events.EventRender2D;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.manager.Theme;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.MathUtility;
import eva.ware.utils.math.Vector4i;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.engine2d.RectUtility;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.BetterText;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TextFormatting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Watermark implements ElementRenderer {

    // ОСТОРОЖНО, ГУЛЯЕТ ЛИШНЯЯ ХРОМОСОМА ПО КОДУ !!! //

    private final BetterText watermarkText = new BetterText(List.of(
            "Тепло и комфортно",
            "Как свет в окне",
            "Неповторимый стиль",
            "Отличный выбор",
            "Кисла как туса <3",
            "Просто великолепно!"
    ), 2000);

    private final BetterText secondWatermarkText = new BetterText(List.of(
            " <3", " >_<", " UwU", " O_O", " OwO", " :>", " <3", " >w<", "~~"
    ), 2000);

    private int fpsAnim, pingAnim;

    @Override
    public void render(EventRender2D eventRender2D) {
        fpsAnim = (int) (Minecraft.getInstance().debugFPS);
        pingAnim = (int) (MathUtility.calculatePing());
        secondWatermarkText.update();
        MatrixStack ms = eventRender2D.getMatrixStack();
        float x = 4;
        float y = 4;
        float padding = 5;
        float fontSize = 15;
        float iconSize = 10;

        if (Evaware.getInst().getModuleManager().getHud().waterMarkMode.is("Табличка")) {
            float margin = 5;
            float off = 2;
            String watermarkTextString = "Евачка " + fpsAnim + "ФПС";
            String secondText = ClientUtility.getGreetingMessage() + ", " + ClientUtility.getUsername() + secondWatermarkText.getOutput().toString();
            float width = Math.max(Fonts.montserrat.getWidth(secondText, 6), Fonts.montserrat.getWidth(watermarkTextString, 8)) + (margin * 1.5F), height = 18;
            RenderUtility.drawStyledShadowRectWithChange(ms, x + off, y + off, width, height);
            RenderUtility.drawRoundedRect(x + off + 1, y + off + 1, 2, height - 2, new Vector4f(3, 3, 0, 0), new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor));
            RenderUtility.drawShadow(x + off + 1, y + off + 1, 2, height - 2, 8, ColorUtility.reAlphaInt(Theme.rectColor, 255), ColorUtility.reAlphaInt(Theme.mainRectColor, 255));
            Fonts.montserrat.drawText(ms, watermarkTextString, x + off + 4.5f, y + off + 1.5f, Theme.textColor, 8, .05f);
            Fonts.montserrat.drawText(ms, secondText, x + off + 4.5f, y + off + 9.5f, Theme.textColor, 6, .025f);
        }

        if (Evaware.getInst().getModuleManager().getHud().waterMarkMode.is("Время")) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            RenderUtility.drawShadow(1, 1, ClientFonts.tech[30].getWidth(formattedTime) + 3, ClientFonts.tech[30].getFontHeight() + 1, 10, ColorUtility.reAlphaInt(Theme.textColor, 90));
            ClientFonts.tech[30].drawString(ms, formattedTime, 3, 6, Theme.textColor);
        }

        if (Evaware.getInst().getModuleManager().getHud().waterMarkMode.is("Обычный")) {
            String text = watermarkText().toString();
            float textWidth =  ClientFonts.msMedium[(int) fontSize].getWidth(text) - 8;
            float width = iconSize + padding + textWidth;
            float height = iconSize + padding;

            RenderUtility.drawStyledShadowRectWithChange(ms, x, y, width, height);
            ClientFonts.msMedium[(int) fontSize].drawString(ms, text, x + padding * 1.5f - 4, y + padding + 0.5f, Theme.textColor);
        }

        if (Evaware.getInst().getModuleManager().getHud().waterMarkMode.is("Плитка")) {
            watermarkText.update();
            float margin = 5;
            float off = 2;
            int bgcolor1 = Theme.rectColor;
            int bgcolor2 = Theme.darkMainRectColor;
            int bgcolor3 = Theme.rectColor;
            int bgcolor4 = Theme.darkMainRectColor;
            float of = 2;
            float pc = 0.1F;
            int white = ColorUtility.getColor(255, 255, 255, 255);

            int color1 = ColorUtility.overCol(white, Theme.rectColor, pc);
            int color2 = ColorUtility.overCol(white, Theme.darkMainRectColor, pc);
            int color3 = ColorUtility.overCol(white, Theme.rectColor, pc);
            int color4 = ColorUtility.overCol(white, Theme.darkMainRectColor, pc);
            String watermarkTextString = "ЕВАЧКА " + fpsAnim + "ФПС";
            String small = watermarkText.getOutput().toString();
            float width = Math.max(ClientFonts.small_pixel[20].getWidth(small), ClientFonts.small_pixel[24].getWidth(watermarkTextString)) + (margin * 1.5F), height = 22;
            RectUtility.getInstance().drawRoundedRectShadowed(eventRender2D.getMatrixStack(), x, y + off, x + width - off, y + height, 2 + 1, 1, bgcolor1, bgcolor2, bgcolor3, bgcolor4, false, true, true, true);
            RectUtility.getInstance().drawRoundedRectShadowed(eventRender2D.getMatrixStack(), x + off, y, x + width, y + height - off, 2, 1, color1, color2, color3, color4, false, true, true, true);

            ClientFonts.small_pixel[24].drawString(eventRender2D.getMatrixStack(), watermarkTextString, x + margin + 0.5, (int) y + 0.5 + off, ColorUtility.multDark(Theme.textColor, 0.5F));
            ClientFonts.small_pixel[24].drawString(eventRender2D.getMatrixStack(), watermarkTextString, x + margin, (int) y + off, ColorUtility.getColor(0, 0, 0, 255));
            ClientFonts.small_pixel[20].drawString(eventRender2D.getMatrixStack(), small, x + margin + 0.25, (int) y + (off + 4f) + height - ClientFonts.small_pixel[28].getFontHeight() - off + 0.25, Theme.textColor);
            ClientFonts.small_pixel[20].drawString(eventRender2D.getMatrixStack(), small, x + margin, (int) y + (off + 4f) + height - ClientFonts.small_pixel[28].getFontHeight() - off, ColorUtility.getColor(20, 20, 20, 255));
        }
    }

    private StringBuilder watermarkText() {
        StringBuilder watermarkText = new StringBuilder();
        watermarkText.append("Evaware").append(" ").append(TextFormatting.GRAY).append(Evaware.edition);

        if (isEnabled("Пользователь") || isEnabled("Фпс") || isEnabled("Пинг") || isEnabled("Сервер") || isEnabled("Пользователь")) {
            watermarkText.append(TextFormatting.DARK_GRAY).append(" | ").append(TextFormatting.WHITE);
        }

        if (isEnabled("Пользователь")) {
            watermarkText.append(ClientUtility.getUsername());

            if (isEnabled("Фпс") || isEnabled("Пинг") || isEnabled("Сервер")) {
                watermarkText.append(TextFormatting.GRAY).append(" - ").append(TextFormatting.WHITE);
            }
        }

        if (isEnabled("Фпс")) {
            watermarkText.append((int) (fpsAnim)).append("fps");
            if (isEnabled("Пинг") || isEnabled("Сервер")) {
                watermarkText.append(TextFormatting.GRAY).append(" - ").append(TextFormatting.WHITE);
            }
        }
        if (isEnabled("Пинг")) {
            watermarkText.append(pingAnim + "ms");

            if (isEnabled("Сервер")) {
                watermarkText.append(TextFormatting.GRAY).append(" - ").append(TextFormatting.WHITE);
            }
        }
        if (isEnabled("Сервер")) {

            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && !mc.getCurrentServerData().serverIP.equals("45.93.200.8:25610")) {
                watermarkText.append(mc.getCurrentServerData().serverIP.toLowerCase());
            } else {
                watermarkText.append("localhost");
            }
        }
        return watermarkText;
    }

    private boolean isEnabled(String check) {
        return Evaware.getInst().getModuleManager().getHud().waterMarkOptions.is(check).getValue();
    }
}
