package newcode.fun.module.impl.display;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.NewCode;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.impl.render.NameTags;
import newcode.fun.module.settings.imp.*;
import newcode.fun.ui.midnight.Style;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.IWrapper;
import newcode.fun.utils.ServerTPS;
import newcode.fun.utils.anim.Animation;
import newcode.fun.utils.anim.Direction;
import newcode.fun.utils.anim.impl.EaseBackIn;
import newcode.fun.control.drag.Dragging;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.font.styled.StyledFont;
import newcode.fun.utils.language.Translated;
import newcode.fun.utils.math.MathUtil;
import newcode.fun.utils.misc.HudUtil;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;
import newcode.fun.utils.render.SmartScissor;
import newcode.fun.utils.render.StencilUtils;
import newcode.fun.utils.render.animation.AnimationMath;
import newcode.fun.utils.tps.TPSServer;
import org.joml.Vector4i;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static newcode.fun.module.impl.player.ItemsCooldown.gappleTime;
import static newcode.fun.utils.render.ColorUtils.rgba;
import static newcode.fun.utils.render.RenderUtils.Render2D.drawCircle;
import static org.joml.Math.lerp;

@Annotation(name = "Interface", type = TypeList.Display)
public class Interface extends Module {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private float smoothFPS = 0;
    private float smoothPING = 0;
    private float smoothX = 0;
    private float smoothY = 0;
    private float smoothZ = 0;

    public final ThemSetting mode = new ThemSetting("Цвет клиента", "1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
    private final TimerUtil timerUtil = new TimerUtil();

    public MultiBoxSetting elements = new MultiBoxSetting("Элементы",
            new BooleanOption("Лого", true),
            new BooleanOption("Радар", true),
            new BooleanOption("Кейбинд", true),
            new BooleanOption("Броня", true),
            new BooleanOption("Уведомления", true),
            new BooleanOption("Список модераторов", true),
            new BooleanOption("Список зелий", true),
            new BooleanOption("Таймер индикатор", true),
            new BooleanOption("Расписание", false),
            new BooleanOption("Таргет Худ", true),
            new BooleanOption("График движение", true));
    public MultiBoxSetting elementwater = new MultiBoxSetting("Элементы лого",
            new BooleanOption("Ник", true),
            new BooleanOption("Айпи", true),
            new BooleanOption("Кординаты", true),
            new BooleanOption("Пинг", true),
            new BooleanOption("Фпс", true),
            new BooleanOption("Тпс", true));
    float CounterEffects;
    private float perc;

    public Interface() {
        addSettings(mode, elements, elementwater);
    }

    @Override
    public boolean onEvent(final Event event) {
        if (mode.is("1")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("1")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("2")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("2")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("3")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("3")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("4")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("4")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("5")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("5")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("6")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("6")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("7")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("7")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("8")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("8")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("9")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("9")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("10")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("10")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("11")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("11")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("12")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("12")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("13")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("13")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (mode.is("14")) {
            for (Style style : Manager.STYLE_MANAGER.styles) {
                if (style.name.equals("14")) {
                    Manager.STYLE_MANAGER.setCurrentStyle(style);
                }
            }
        }
        if (event instanceof EventRender eventRender) {
            if (eventRender.isRender2D()) {
                handleRender(eventRender);
            }
        }
        return false;
    }


    private void handleRender(EventRender renderEvent) {
        final MatrixStack stack = renderEvent.matrixStack;

        if (elements.get(0)) {
            WaterMark(stack);
        }
        if (elements.get(1)) {
            Radar(stack);
        }
        if (elements.get(5)) {
            onRenderStaffActive(stack);
        }
        if (elements.get(6)) {
            onPotionElementsRender(stack, renderEvent);
        }
        if (elements.get(7)) {
            onRenderTimerHUD(stack);
        }
        if (elements.get(8)) {
            onEventsRender(stack);
        }
        if (elements.get(9)) {
            onRenderTargetHUD(stack);
        }
        if (elements.get(2)) {
            onKeyBindsRender(stack);
        }
        if (elements.get(3)) {
            onArmorRender(renderEvent);
        }
        if (elements.get(10)) {
            onGrapshikRender(stack);
        }
        if (Manager.FUNCTION_MANAGER.gappleCooldownFunction.state && Manager.FUNCTION_MANAGER.gappleCooldownFunction.visuals.get()) {
            onColdownRender(stack);
        }
    }

    public Dragging Coldown = NewCode.createDrag(this, "onColdownRender", sr.scaledWidth() / 2, sr.scaledHeight() / 2 + 120);

    private void onColdownRender(final MatrixStack stack) {
        float x = this.Coldown.getX();
        float y = this.Coldown.getY();
        int width = 20;
        float height = 20;
        this.Coldown.setWidth(width);
        this.Coldown.setHeight(height);
        Long lastUseTime = Manager.FUNCTION_MANAGER.gappleCooldownFunction.lastUseItemTime.get(Items.GOLDEN_APPLE);

        if (lastUseTime == null || (System.currentTimeMillis() - lastUseTime) / 1000.0f >= gappleTime.getValue().floatValue()) {
            if (this.mc.currentScreen instanceof ChatScreen) {
                IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 10, ColorUtils.rgba(20, 20, 20, 200)));
                RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 10, ColorUtils.rgba(20, 20, 20, 200));
                RenderUtils.Render2D.drawImage(new ResourceLocation("textures/item/golden_apple.png"), x + 2, y + 2, 16, 16, -1);
                drawCircle(
                        x + 10f, y + 10f,
                        0,
                        360,
                        9,
                        3,
                        false,
                        ColorUtils.rgba(16, 16, 16, 255)
                );
            }
        }

        if (lastUseTime == null || (System.currentTimeMillis() - lastUseTime) / 1000.0f >= gappleTime.getValue().floatValue()) return;
        if (ClientUtils.isPvP()) {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 10, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 10, ColorUtils.rgba(20, 20, 20, 200));
            RenderUtils.Render2D.drawImage(new ResourceLocation("textures/item/golden_apple.png"), x + 2, y + 2, 16, 16, -1);
            if (lastUseTime == null) return;

            float elapsed = (System.currentTimeMillis() - lastUseTime) / 1000.0f;
            float maxCooldown = (float) gappleTime.getValue();
            float progress = 1.0f - Math.min(elapsed / maxCooldown, 1.0f);
            drawCircle(
                    x + 10f, y + 10f,
                    0,
                    360,
                    9,
                    3,
                    false,
                    ColorUtils.rgba(16, 16, 16, 255)
            );
            drawCircle(
                    x + 10f, y + 10f,
                    0,
                    360 * progress,
                    9,
                    3,
                    false,
                    Manager.STYLE_MANAGER.getCurrentStyle()
            );

            float remainingTime = maxCooldown - elapsed;
            String formattedTime = String.format(Locale.US, "%.1f", remainingTime); // Используем Locale.US, чтобы всегда использовать точку
            Fonts.newcode[12].drawCenteredString(stack, formattedTime, x + 9.9f, y + 25, -1);
        }
    }


    private String countdownText = "";
    private String countdownText2 = "";
    private String countdownText3 = "";

    private void updateCountdown2() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        LocalDateTime targetTime = now.withHour(15).withMinute(30).withSecond(0);

        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }

        long hours = ChronoUnit.HOURS.between(now, targetTime);
        long minutes = ChronoUnit.MINUTES.between(now, targetTime) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, targetTime) % 60;

        StringBuilder countdownBuilder = new StringBuilder();

        if (hours > 0) {
            countdownBuilder.append(hours).append("h ");
        }
        if (minutes > 0) {
            countdownBuilder.append(minutes).append("m ");
        }
        countdownBuilder.append(seconds).append("s");

        countdownText3 = countdownBuilder.toString().trim();
    }

    private void updateCountdown3() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        LocalDateTime targetTime = now.withHour(19).withMinute(30).withSecond(0);

        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }

        long hours = ChronoUnit.HOURS.between(now, targetTime);
        long minutes = ChronoUnit.MINUTES.between(now, targetTime) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, targetTime) % 60;

        StringBuilder countdownBuilder = new StringBuilder();

        if (hours > 0) {
            countdownBuilder.append(hours).append("h ");
        }
        if (minutes > 0) {
            countdownBuilder.append(minutes).append("m ");
        }
        countdownBuilder.append(seconds).append("s");

        countdownText2 = countdownBuilder.toString().trim();
    }

    private void updateCountdown() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        List<LocalDateTime> schedule = Arrays.asList(
                now.withHour(9).withMinute(0).withSecond(0),
                now.withHour(11).withMinute(0).withSecond(0),
                now.withHour(13).withMinute(0).withSecond(0),
                now.withHour(15).withMinute(0).withSecond(0),
                now.withHour(17).withMinute(0).withSecond(0),
                now.withHour(19).withMinute(0).withSecond(0),
                now.withHour(21).withMinute(0).withSecond(0),
                now.withHour(23).withMinute(0).withSecond(0)
        );


        LocalDateTime nextEventTime = schedule.stream()
                .filter(t -> t.isAfter(now))
                .findFirst()
                .orElse(now.withHour(7).plusDays(1));

        long hours = ChronoUnit.HOURS.between(now, nextEventTime);
        long minutes = ChronoUnit.MINUTES.between(now, nextEventTime) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, nextEventTime) % 60;

        StringBuilder countdownBuilder = new StringBuilder();

        if (hours > 0) {
            countdownBuilder.append(hours).append("h ");
        }
        if (minutes > 0) {
            countdownBuilder.append(minutes).append("m ");
        }
        countdownBuilder.append(seconds).append("s");

        countdownText = countdownBuilder.toString().trim();
    }

    public Dragging events = NewCode.createDrag(this, "onEventsRender", 350, 55);

    private volatile List<String> cachedLines = Arrays.asList("null", "0", "null", "0", "null", "0"); // Кешированные данные
    private boolean isFetching = false;

    private void onEventsRender(MatrixStack stack) {
        float posX = events.getX();
        float posY = events.getY();
        int headerHeight = 15;
        int padding = 5;
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());

        int width = 84;
        float height = 30;

        updateCountdown();
        updateCountdown2();
        updateCountdown3();

        if (!ClientUtils.isConnectedToServer("funtime")) {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2, height + 15, 4, ColorUtils.rgba(20, 20, 20, 200)));

            RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2, height + 15, 4, ColorUtils.rgba(20, 20, 20, 200));
                Fonts.newcode[16].drawCenteredString(stack, "Events", posX + 26, posY + 7.3f, -1);
                Fonts.icon[18].drawCenteredString(stack, "g", posX + 8, posY + 8, firstColor2);
                Fonts.newcode[13].drawString(stack, "AirDrop", posX + padding - 0.7f, posY + headerHeight + padding, -1);
                Fonts.newcode[13].drawString(stack, countdownText, posX + width - Fonts.newcode[13].getWidth(countdownText) - padding + 0.9f, posY + headerHeight + padding, -1);
                Fonts.newcode[13].drawString(stack, "Mascot", posX + padding - 0.7f, posY + headerHeight + padding + 10, -1);
                Fonts.newcode[13].drawString(stack, countdownText2, posX + width - Fonts.newcode[13].getWidth(countdownText2) - padding + 0.9f, posY + headerHeight + padding + 10, -1);
                Fonts.newcode[13].drawString(stack, "Scrooge", posX + padding - 0.7f, posY + headerHeight + padding + 20, -1);
                Fonts.newcode[13].drawString(stack, countdownText3, posX + width - Fonts.newcode[13].getWidth(countdownText3) - padding + 0.9f, posY + headerHeight + padding + 20, -1);

        } else {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2, height + 15, 4, ColorUtils.rgba(20, 20, 20, 200)));

                RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2, height + 15, 4, ColorUtils.rgba(20, 20, 20, 200));

            Fonts.newcode[16].drawCenteredString(stack, "Events", posX + 26, posY + 7.3f, -1);
            Fonts.icon[18].drawCenteredString(stack, "g", posX + 8, posY + 8, firstColor2);
            List<String> lastLines = getLastNLines(cachedLines, 3);
            for (String line : lastLines) {
                if (line == null || line.trim().isEmpty() || line.contains("null")) {
                    line = "0 0";
                }
            }

            Fonts.newcode[13].drawString(stack, "Anarchy-" + lastLines.get(0).split(" ")[0],
                    posX + padding - 0.7f, posY + headerHeight + padding, -1);
            int totalSeconds = Integer.parseInt(getAnarchyNumber(lastLines.get(0)));
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String timeFormatted = (minutes > 0 ? minutes + "m " : "") + seconds + "s";

            Fonts.newcode[13].drawString(stack, timeFormatted,
                    posX + width - Fonts.newcode[13].getWidth(timeFormatted) - padding + 0.9f,
                    posY + headerHeight + padding, -1);

            Fonts.newcode[13].drawString(stack, "Anarchy-" + lastLines.get(1).split(" ")[0],
                    posX + padding - 0.7f, posY + headerHeight + padding + 10, -1);
            int totalSeconds1 = Integer.parseInt(getAnarchyNumber(lastLines.get(1)));
            int minutes1 = totalSeconds1 / 60;
            int seconds1 = totalSeconds1 % 60;
            String timeFormatted1 = (minutes1 > 0 ? minutes1 + "m " : "") + seconds1 + "s";

            Fonts.newcode[13].drawString(stack, timeFormatted1,
                    posX + width - Fonts.newcode[13].getWidth(timeFormatted1) - padding + 0.9f,
                    posY + headerHeight + padding + 10, -1);

            Fonts.newcode[13].drawString(stack, "Anarchy-" + lastLines.get(2).split(" ")[0],
                    posX + padding - 0.7f, posY + headerHeight + padding + 20, -1);
            int totalSeconds2 = Integer.parseInt(getAnarchyNumber(lastLines.get(2)));
            int minutes2 = totalSeconds2 / 60;
            int seconds2 = totalSeconds2 % 60;
            String timeFormatted2 = (minutes2 > 0 ? minutes2 + "m " : "") + seconds2 + "s";

            Fonts.newcode[13].drawString(stack, timeFormatted2,
                    posX + width - Fonts.newcode[13].getWidth(timeFormatted2) - padding + 0.9f,
                    posY + headerHeight + padding + 20, -1);


            if (!isFetching) {
                isFetching = true;
                new Thread(() -> {
                    try {
                        URL url = new URL("http://193.233.113.133:5000/");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        List<String> newLines = new ArrayList<>();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            newLines.add(line.replace("</pre>", "").replace("<pre>", "").replace(":", "  "));
                        }

                        reader.close();

                        if (newLines.size() >= 6) {
                            cachedLines = newLines;
                        }

                    } catch (IOException e) {
                        System.out.println("Ошибка запроса: " + e.getMessage());
                        cachedLines = Arrays.asList("Error", "0", "Error", "0", "Error", "0");
                    } finally {
                        isFetching = false;
                    }
                }).start();
            }
        }

        events.setWidth(width);
        events.setHeight(height);
    }

    private List<String> getLastNLines(List<String> lines, int n) {
        int size = lines.size();
        return size >= n ? lines.subList(size - n, size) : lines;
    }

    private String getAnarchyNumber(String input) {
        String[] parts = input.split(" ");
        String numberPart = parts.length > 1 ? parts[parts.length - 1] : parts[0];

        if (numberPart.equals("null") || !numberPart.matches("\\d+")) {
            return "0";
        }

        return numberPart;
    }


    public Dragging Grapshik = NewCode.createDrag(this, "Grapshik", 360, 260);

    private float[] graphValues = new float[30];
    private int currentIndex = 0;
    private float smoothedBps = 0;
    private int updateInterval = 5;
    private int frameCounter = 0;

    private void updateGraphValues() {
        double distance = Math.sqrt(Math.pow(mc.player.getPosX() - mc.player.prevPosX, 2) + Math.pow(mc.player.getPosY() - mc.player.prevPosY, 2) + Math.pow(mc.player.getPosZ() - mc.player.prevPosZ, 2));
        float bps = (float) (distance * mc.timer.timerSpeed * 20.0D);
        bps = Math.min(bps, 10);

        smoothedBps = smoothedBps + (bps - smoothedBps) * 0.1f;

        if (frameCounter++ >= updateInterval) {
            graphValues[currentIndex] = smoothedBps;
            currentIndex = (currentIndex + 1) % graphValues.length;
            frameCounter = 0;
        }
    }

    private void onGrapshikRender(final MatrixStack stack) {
        float x = this.Grapshik.getX() - 2.5f;
        float y = this.Grapshik.getY();
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        String bps = HudUtil.calculateBPS() + "";

        StyledFont newcode = Fonts.newcode[11];

        float width = 90;
        float height = 33;

        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x  + 4f, y + 2.5f, width - 8 , height - 5, 5, ColorUtils.rgba(20, 20, 20, 200)));
        RenderUtils.Render2D.drawRoundedRect(x  + 4f, y + 2.5f, width - 8 , height - 5, 5, ColorUtils.rgba(20, 20, 20, 200));

        int graphPoints = graphValues.length;

        float graphStartX = x + 5;
        float graphStartY = y + height - 5;
        float graphWidth = width - 10;
        float graphHeight = height - 10;

        for (int i = 0; i < graphPoints - 1; i++) {
            int index1 = (currentIndex + i) % graphPoints;
            int index2 = (currentIndex + i + 1) % graphPoints;

            float x1 = graphStartX + (i / (float)(graphPoints - 1)) * graphWidth;
            float y1 = graphStartY - (graphValues[index1] / 10f) * graphHeight; // Нормализация (максимум 10 BPS)
            float x2 = graphStartX + ((i + 1) / (float)(graphPoints - 1)) * graphWidth;
            float y2 = graphStartY - (graphValues[index2] / 10f) * graphHeight;

            RenderUtils.Render2D.drawLine(x1, y1, x2, y2, firstColor2);
        }
        newcode.drawString(stack, bps + " bps", x + 85 - newcode.getWidth(bps + " bps"), y - 1F, -1);

        this.Grapshik.setWidth(width - 5);
        this.Grapshik.setHeight(height - 5);

        updateGraphValues();
    }


    public Dragging RadarHUD = NewCode.createDrag(this, "RadarHUD", 200, 200);
    private final Minecraft mc = Minecraft.getInstance();
    private float radarSize = 50;
    private float maxDistance = 47.0f;
    private void Radar(MatrixStack stack) {
        float x = this.RadarHUD.getX();
        float y = this.RadarHUD.getY();
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        float width = 50.0F;
        float height = 50.0F;
        float centerX = this.RadarHUD.getX();
        float centerY = this.RadarHUD.getY();
        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 5, ColorUtils.rgba(20, 20, 20, 200)));
        RenderUtils.Render2D.drawRoundedRect(x, y, width, height, 5, ColorUtils.rgba(20, 20, 20, 200));
        RenderUtils.Render2D.drawShadow(x + 25, y + 25, width - 25, 0.5f, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 0));
        RenderUtils.Render2D.drawShadow(x, y + 25, width - 25, 0.5f, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 255));
        RenderUtils.Render2D.drawShadow(x + 25, y + 25, 0.5f, height - 25, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
        RenderUtils.Render2D.drawShadow(x + 25, y, 0.5f, height - 25, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));
        SmartScissor.push();
        SmartScissor.setFromComponentCoordinates(x, y, width, height);
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            double deltaX = player.getPosX() - mc.player.getPosX();
            double deltaZ = player.getPosZ() - mc.player.getPosZ();

            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (distance > maxDistance) continue;

            double angle = Math.atan2(deltaZ, deltaX) - Math.toRadians(mc.player.rotationYaw + 180);
            double scaledDistance = (distance / maxDistance) * (radarSize / 2);

            float radarPlayerX = (float) (centerX + scaledDistance * Math.cos(angle));
            float radarPlayerY = (float) (centerY + scaledDistance * Math.sin(angle));
            RenderUtils.Render2D.drawRoundOutline(radarPlayerX - 1.5f + 25, radarPlayerY - 0.5f + 25, 3, 3, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140), ColorUtils.setAlpha(-1, 140)));
            RenderUtils.Render2D.drawRoundedRect(radarPlayerX - 1f + 25, radarPlayerY + 25, 2, 2f, 0, -1);
        }
        SmartScissor.unset();
        SmartScissor.pop();


        this.RadarHUD.setWidth(width);
        this.RadarHUD.setHeight(height);
    }

    private void WaterMark(MatrixStack stack) {
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        String name = Manager.USER_PROFILE.getName();
        String server;
        if (mc.getInstance().getCurrentServerData() != null) {
            server = mc.getCurrentServerData().serverIP;
        } else {
            server = "local";
        }
        int targetFPS = mc.debugFPS;
        smoothFPS = lerp(smoothFPS, targetFPS, 0.055f);

        int targetPING = HudUtil.calculatePing();
        smoothPING = lerp(smoothPING, targetPING, 0.055f);

        int targetZ = (int) IMinecraft.mc.player.getPosZ();
        smoothZ = lerp(smoothZ, targetZ, 0.055f);

        int targetY = (int) IMinecraft.mc.player.getPosY();
        smoothY = lerp(smoothY, targetY, 0.055f);

        int targetX = (int) IMinecraft.mc.player.getPosX();
        smoothX = lerp(smoothX, targetX, 0.055f);

        String ping = (int) smoothPING + "ms";
        String fps = (int) smoothFPS + "fps";
        String tps = ServerTPS.getTPS() + "tps";
        String cord = "X: " + (int) smoothX + " Y: " + (int) smoothY + " Z: " + (int) smoothZ;

        StyledFont newcode = Fonts.newcode[14];
        float titleWidth = newcode.getWidth(name) + 20.0F;
        float title2Width = newcode.getWidth(server) + 20.0F;
        float title3Width = newcode.getWidth(ping) + 20.0F;
        float title4Width = newcode.getWidth(fps) + 20.0F;
        float title5Width = newcode.getWidth(tps) + 20.0F;
        float title6Width = newcode.getWidth(cord) + 20.0F;
        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(19, 5, (elementwater.get(0) ? titleWidth : 0) + (elementwater.get(1) ? title2Width : 0) + (elementwater.get(2) ? title6Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200)));

        RenderUtils.Render2D.drawRoundedRect(19, 5, (elementwater.get(0) ? titleWidth : 0) + (elementwater.get(1) ? title2Width : 0) + (elementwater.get(2) ? title6Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200));

        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(5, 5, 13, 13, 5, ColorUtils.rgba(20, 20, 20, 200)));

        RenderUtils.Render2D.drawRoundedRect(5, 5, 13, 13, 5, ColorUtils.rgba(20, 20, 20, 200));

        RenderUtils.Render2D.drawRoundOutline(5, 5, 13, 13, 5, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(firstColor2, firstColor2, firstColor2, firstColor2));

        Fonts.icon2[17].drawCenteredString(stack, "a", 12F, 10.2F, firstColor2);
        Fonts.icon2[17].drawCenteredString(stack, "b", 12F, 10.2F, firstColor2);
        if (elementwater.get(0) && (elementwater.get(1) || elementwater.get(2))) {
            newcode.drawString(stack, TextFormatting.GRAY + "*", 33F + titleWidth - 17, 11.5F, -1);
        }
        if (elementwater.get(0)) {
            newcode.drawString(stack, name, 33F, 10F, -1);
            Fonts.icon[15].drawCenteredString(stack, "a", 27F, 10.5F, firstColor2);
        }
        if (elementwater.get(1)) {
            Fonts.icon[15].drawCenteredString(stack, "b", 33F + (elementwater.get(0) ? titleWidth : 0) - 6, 10.5F, firstColor2);
            newcode.drawString(stack, server, 33F + (elementwater.get(0) ? titleWidth : 0), 10F, -1);
        }
        if (elementwater.get(2) && (elementwater.get(0) || elementwater.get(1))) {
            newcode.drawString(stack, TextFormatting.GRAY + "*", 33F + (elementwater.get(0) ? titleWidth : 0) + (elementwater.get(1) ? title2Width : 0) - 17, 11.5F, -1);
        }
        if (elementwater.get(2)) {
            Fonts.icon[15].drawCenteredString(stack, "r", 33F + (elementwater.get(0) ? titleWidth : 0) + (elementwater.get(1) ? title2Width : 0) - 6, 10.5F, firstColor2);
            newcode.drawString(stack, cord, 33F + (elementwater.get(0) ? titleWidth : 0) + (elementwater.get(1) ? title2Width : 0), 10F, -1);
        }

        if (elementwater.get(0) || elementwater.get(1) || elementwater.get(2)) {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(5, 20, (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + (elementwater.get(5) ? title5Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(5, 20, (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + (elementwater.get(5) ? title5Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200));
            if (elementwater.get(3) && (elementwater.get(4) || elementwater.get(5))) {
                newcode.drawString(stack, TextFormatting.GRAY + "*", 14 + title3Width - 11, 26.5F, -1);
            }
            if (elementwater.get(3)) {
                Fonts.icon[15].drawCenteredString(stack, "c", 14, 25.5F, firstColor2);
                newcode.drawString(stack, ping, 20, 25F, -1);
            }
            if (elementwater.get(4)) {
                Fonts.icon[15].drawCenteredString(stack, "d", 14 + (elementwater.get(3) ? title3Width : 0), 25.5F, firstColor2);
                newcode.drawString(stack, fps, 14 + (elementwater.get(3) ? title3Width : 0) + 6, 25F, -1);
            }
            if (elementwater.get(4) && (elementwater.get(5))) {
                newcode.drawString(stack, TextFormatting.GRAY + "*", 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) - 11, 26.5F, -1);
            }
            if (elementwater.get(5)) {
                Fonts.icon[15].drawCenteredString(stack, "e", 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0), 25.5F, firstColor2);
                newcode.drawString(stack, tps, 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + 6, 25F, -1);
            }
        } else {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(5 + 14, 20 - 15, (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + (elementwater.get(5) ? title5Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(5 + 14, 20 - 15, (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + (elementwater.get(5) ? title5Width : 0), 13, 5, ColorUtils.rgba(20, 20, 20, 200));
            if (elementwater.get(3) && (elementwater.get(4) || elementwater.get(5))) {
                newcode.drawString(stack, TextFormatting.GRAY + "*", 14 + title3Width - 11 + 14, 26.5F - 15, -1);
            }
            if (elementwater.get(3)) {
                Fonts.icon[15].drawCenteredString(stack, "c", 14 + 14, 25.5F - 15, firstColor2);
                newcode.drawString(stack, ping, 20 + 14, 25F - 15, -1);
            }
            if (elementwater.get(4)) {
                Fonts.icon[15].drawCenteredString(stack, "d", 14 + (elementwater.get(3) ? title3Width : 0) + 14, 25.5F - 15, firstColor2);
                newcode.drawString(stack, fps, 14 + (elementwater.get(3) ? title3Width : 0) + 6 + 14, 25F - 15, -1);
            }
            if (elementwater.get(4) && (elementwater.get(5))) {
                newcode.drawString(stack, TextFormatting.GRAY + "*", 14 + 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) - 11, 26.5F - 15, -1);
            }
            if (elementwater.get(5)) {
                Fonts.icon[15].drawCenteredString(stack, "e", 14 + 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0), 25.5F - 15, firstColor2);
                newcode.drawString(stack, tps, 14 + 14 + (elementwater.get(3) ? title3Width : 0) + (elementwater.get(4) ? title4Width : 0) + 6, 25F - 15, -1);
            }
        }
    }


    public Dragging TimerHUD = NewCode.createDrag(this, "TimerHUD", 160, 180);

    private void onRenderTimerHUD(MatrixStack stack) {
        float x = this.TimerHUD.getX();
        float y = this.TimerHUD.getY();
        float quotient = Manager.FUNCTION_MANAGER.timer.maxViolation / Manager.FUNCTION_MANAGER.timer.timerAmount.getValue().floatValue();
        float minimumValue = Math.min(Manager.FUNCTION_MANAGER.timer.getViolation(), quotient);
        this.perc = AnimationMath.lerp(this.perc, (quotient - minimumValue) / quotient, 10.0F);
        float width = 50.0F;
        float height = 4.0F;
        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x - 2.5f, y - 2.4f + 1, width + 5, height + 5, 3, ColorUtils.rgba(20, 20, 20, 200)));
        RenderUtils.Render2D.drawRoundedRect(x - 2.5f, y - 2.4f + 1, width + 5, height + 5,  3, ColorUtils.rgba(20, 20, 20, 200));

        RenderUtils.Render2D.drawGradientRound(x + 1, y + 1 + 1, width - 2, height - 2, 1.0F, ColorUtils.rgba(41, 41, 41, 255), ColorUtils.rgba(41, 41, 41, 255), ColorUtils.rgba(41, 41, 41, 255), ColorUtils.rgba(41, 41, 41, 255));
        RenderUtils.Render2D.drawGradientRound(x + 1, y + 1 + 1, width * this.perc - 2, height - 2, 1.0F, ColorUtils.getColorStyle(0.0F), ColorUtils.getColorStyle(90.0F), ColorUtils.getColorStyle(180.0F), ColorUtils.getColorStyle(270.0F));

        this.TimerHUD.setWidth(width + 2);
        this.TimerHUD.setHeight(height);
    }

    private float heightDynamic = 0;
    private int activeModules = 0;
    private final Animation KeyBindsAnimation = new EaseBackIn(200, 1, 1.5f);

    public Dragging keyBinds = NewCode.createDrag(this, "KeyBinds", 120, 95);

    private void onKeyBindsRender(MatrixStack stack) {
        float maxNameWidth = 0;
        float maxBindWidth = 0;
        float posX = keyBinds.getX();
        float posY = keyBinds.getY();
        this.KeyBindsAnimation.setDuration(300);
        this.scale = KeyBindsAnimation.getOutput();
        int headerHeight = 15;
        int padding = 5;
        int offset = 10;
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

        for (Module f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f.bind != 0 && f.state) {
                String functionName = f.name;
                float nameWidth = Fonts.newcode[16].getWidth(functionName);

                maxNameWidth = Math.max(maxNameWidth, nameWidth);

                String bindText = TextFormatting.GRAY + ClientUtils.getKey(f.bind).toUpperCase();
                float bindWidth = Fonts.newcode[13].getWidth(bindText);
                maxBindWidth = Math.max(maxBindWidth, bindWidth);
            }
        }


        int minWidth = Math.max(72, (int) (maxNameWidth + maxBindWidth + 1 * padding) - 13);
        this.widthDynamic = AnimationMath.fast(this.widthDynamic, minWidth, 15);
        int width = (int) ((int) widthDynamic + 1.5f);

        StyledFont newcode = Fonts.newcode[13];

        float height = activeModules * offset;
        this.heightDynamic = AnimationMath.fast(this.heightDynamic, height, 15);

        if (scale == 0.0F) {
            activeModules = 0;
        }


        if (activeModules == 0 &&  this.mc.currentScreen instanceof ChatScreen) {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 6, height + 13, 4, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 6, height + 13, 4, ColorUtils.rgba(20, 20, 20, 200));

            Fonts.newcode[16].drawCenteredString(stack, "Binds", keyBinds.getX() + 25, posY + 7.3f, -1);
            Fonts.icon[18].drawCenteredString(stack, "f", keyBinds.getX() + 8, posY + 8, firstColor2);
        }


        if (activeModules > 0) {
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2 - 4, height + 15,  4, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 2 - 4, height + 16, 4, ColorUtils.rgba(20, 20, 20, 200));

            Fonts.newcode[16].drawCenteredString(stack, "Binds", keyBinds.getX() + 25, posY + 7.3f, -1);
            Fonts.icon[18].drawCenteredString(stack, "f", keyBinds.getX() + 8, posY + 8, firstColor2);
        }

        SmartScissor.push();
        SmartScissor.setFromComponentCoordinates(keyBinds.getX(), keyBinds.getY() + 17, width, headerHeight + heightDynamic + padding / 2f);
        int index = 0;

        for (Module f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f.bind != 0 && f.state) {
                String text = ClientUtils.getKey(f.bind);
                if (text == null) {
                    continue;
                }
                text = text.substring(0, 1);

                RenderUtils.Render2D.drawRoundedRect(posX + width - padding - 12.4f, posY + (index * offset) + 17, 9.5f, 9.5f, 3, ColorUtils.rgba(30, 30, 30, 200));
                if (f.state) {
                    RenderUtils.Render2D.drawRoundOutline(posX + width - padding - 12.4f, posY + (index * offset) + 17, 9.5f, 9.5f, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(firstColor2, firstColor2, firstColor2, firstColor2));
                } else {
                    RenderUtils.Render2D.drawRoundOutline(posX + width - padding - 12.4f, posY + (index * offset) + 17, 9.5f, 9.5f, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(60, 60, 60, 230).getRGB(), new Color(60, 60, 60, 230).getRGB(), new Color(60, 60, 60, 230).getRGB(), new Color(60, 60, 60, 230).getRGB()));
                }
                Fonts.newcode[13].drawString(stack, f.name, posX + padding - 0.7f, posY + headerHeight + padding + (index * offset), -1);
                newcode.drawString(stack, text.toUpperCase(), posX + width - padding - 7.6f - (newcode.getWidth(text.toUpperCase()) / 2), posY + headerHeight + padding + (index * offset) + 0.5f, -1);

                index++;
            }
        }
        SmartScissor.unset();
        SmartScissor.pop();
        activeModules = index;

        keyBinds.setWidth(width - 4);
        keyBinds.setHeight(activeModules * offset + headerHeight + 1);
    }

    public Dragging ActiveStaff = NewCode.createDrag(this, "ActiveStaff", 120, 95);
    public CopyOnWriteArrayList<net.minecraft.util.text.TextComponent> components = new CopyOnWriteArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private int activeStaff = 0;
    private float hDynam = 0;
    private float widthDynamic = 0;
    private float nameWidth = 0;
    int index = 0;

    private void onRenderStaffActive(MatrixStack stack) {
        float posX = ActiveStaff.getX();
        float posY = ActiveStaff.getY();
        int headerHeight = 15;
        float width = 75;
        int padding = 5;
        int offset = 10;
        float height = activeStaff * offset;
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

        this.hDynam = AnimationMath.fast(this.hDynam, height, 10);
        this.widthDynamic = AnimationMath.fast(this.widthDynamic, width, 10);

        if (activeStaff == 0 && this.mc.currentScreen instanceof ChatScreen) {
            float finalPosY = posY;
            float finalHeight = height;
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, finalPosY + 3, width - 6, finalHeight + 13, 4, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width - 6, height + 13, 4, ColorUtils.rgba(20, 20, 20, 200));

            Fonts.newcode[16].drawCenteredString(stack, "Staffs", posX + 25, posY + 7.3f, -1);
            Fonts.icon[18].drawCenteredString(stack, "h", posX + 8, posY + 8, firstColor2);
        }

        if (activeStaff > 0) {
            float finalHeight1 = height;
            float finalPosY1 = posY;
            IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(posX + 1, finalPosY1 + 3, width, finalHeight1 + 15, 4, ColorUtils.rgba(20, 20, 20, 200)));
            RenderUtils.Render2D.drawRoundedRect(posX + 1, posY + 3, width, height + 15, 4, ColorUtils.rgba(20, 20, 20, 200));

            Fonts.newcode[16].drawCenteredString(stack, "Staffs", posX + 25, posY + 7.3f, -1);
            Fonts.icon[18].drawCenteredString(stack, "h", posX + 8, posY + 8, firstColor2);
        }

        int i = 0;

        if (mc.world == null || mc.getConnection() == null) {
            return;
        }

        Iterator<ScorePlayerTeam> teams = mc.world.getScoreboard().getTeams().stream()
                .sorted(Comparator.comparing(Team::getName))
                .toList().iterator();

        while (teams.hasNext()) {
            ScorePlayerTeam team = teams.next();

            String name = team.getMembershipCollection().toString();
            if (name.isEmpty() || name.length() <= 2) {
                continue;
            }
            name = name.substring(1, name.length() - 1);

            if (name.equalsIgnoreCase("portal") || name.equalsIgnoreCase("anarchy") || name.equalsIgnoreCase("grief") || name.equalsIgnoreCase("kitbog") || name.equalsIgnoreCase("bog") || name.equalsIgnoreCase("free") || name.equalsIgnoreCase("effecter") || name.equalsIgnoreCase("battlepass") || name.equalsIgnoreCase("conductor") || name.equalsIgnoreCase("soul")) {
                continue;
            }

            if (!namePattern.matcher(name).matches()) {
                continue;
            }

            boolean vanish = true;
            boolean near = false;
            boolean active = false;

            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (player.getNameClear().equals(name)) {
                    near = true;
                }
            }

            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                    if (!near) {
                        active = true;
                    }
                }
            }

            if (!vanish && !Manager.STAFF_MANAGER.isStaff(name)) {
                continue;
            }

            String Name = name.substring(0, Math.min(name.length(), 13));

            if (Manager.STAFF_MANAGER.isStaff(name)) {
                Name = TextFormatting.BLUE + "MOD " + TextFormatting.WHITE + Name;
            }

            if (i >= 10) {
                break;
            }

            SmartScissor.push();
            SmartScissor.setFromComponentCoordinates(posX, posY, width - 13f, headerHeight + hDynam + padding / 2f);

            Fonts.newcode[13].drawString(stack, TextFormatting.WHITE + Name, posX + padding, posY + headerHeight + padding + (index * offset) + 0.4f, -1);

            SmartScissor.unset();
            SmartScissor.pop();

            nameWidth = Fonts.newcode[13].getWidth(name);

            if (active) {
                RenderUtils.Render2D.drawRoundOutline(posX + width - 8 + 0.5f, posY + headerHeight + padding + (index * offset) - 1 + 0.5f, 4, 4, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(ColorUtils.setAlpha(new Color(112, 112, 112).getRGB(), 140), ColorUtils.setAlpha(new Color(112, 112, 112).getRGB(), 140), ColorUtils.setAlpha(new Color(112, 112, 112).getRGB(), 140), ColorUtils.setAlpha(new Color(112, 112, 112).getRGB(), 140)));
                RenderUtils.Render2D.drawRoundedRect(posX + width - 8 + 1f, posY + headerHeight + padding + (index * offset) - 1 + 1f, 3, 3f, 1, new Color(112, 112, 112).getRGB());
            }

            if (vanish) {
                RenderUtils.Render2D.drawRoundOutline(posX + width - 8 + 0.5f, posY + headerHeight + padding + (index * offset) - 1 + 0.5f, 4, 4, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(ColorUtils.setAlpha(new Color(202, 40, 40).getRGB(), 140), ColorUtils.setAlpha(new Color(202, 40, 40).getRGB(), 140), ColorUtils.setAlpha(new Color(202, 40, 40).getRGB(), 140), ColorUtils.setAlpha(new Color(202, 40, 40).getRGB(), 140)));
                RenderUtils.Render2D.drawRoundedRect(posX + width - 8 + 1f, posY + headerHeight + padding + (index * offset) - 1 + 1f, 3, 3f, 1, new Color(202, 40, 40).getRGB());
            }

            if (near && !vanish) {
                RenderUtils.Render2D.drawRoundOutline(posX + width - 8 + 0.5f, posY + headerHeight + padding + (index * offset) - 1 + 0.5f, 4, 4, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(ColorUtils.setAlpha(new Color(193, 107, 46).getRGB(), 140), ColorUtils.setAlpha(new Color(193, 107, 46).getRGB(), 140), ColorUtils.setAlpha(new Color(193, 107, 46).getRGB(), 140), ColorUtils.setAlpha(new Color(193, 107, 46).getRGB(), 140)));
                RenderUtils.Render2D.drawRoundedRect(posX + width - 8 + 1f, posY + headerHeight + padding + (index * offset) - 1 + 1f, 3, 3f, 1, new Color(193, 107, 46).getRGB());
            }

            posY += offset;
            ++i;
        }

        this.activeStaff = i;
        ActiveStaff.setWidth(widthDynamic);
        ActiveStaff.setHeight(hDynam + headerHeight);
    }

    private void onArmorRender(final EventRender renderEvent) {
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == Items.TOTEM_OF_UNDYING) {
                count++;
            }
        }
        float xPos = renderEvent.scaledResolution.scaledWidth() / 2f;
        float yPos = renderEvent.scaledResolution.scaledHeight();

        boolean totemInInv = mc.player.inventory.mainInventory.stream().map(ItemStack::getItem).toList().contains(Items.TOTEM_OF_UNDYING);
        int off = totemInInv ? + 5 : 0;
        if (mc.player.isCreative()) {
            yPos += 17;
        }

        for (ItemStack s : mc.player.inventory.armorInventory) {
            NameTags.drawItemStack(s, xPos - off + 74 * (mc.gameSettings.guiScale / 2f), yPos - 56 * (mc.gameSettings.guiScale / 2f), null, false);
            off += 15;
        }
        if (totemInInv)
            NameTags.drawItemStack(new ItemStack(Items.TOTEM_OF_UNDYING), xPos - off + 73 * (mc.gameSettings.guiScale / 2f), yPos - 56 * (mc.gameSettings.guiScale / 2f), String.valueOf(count), false);
    }

    public final Dragging HUDPotion = NewCode.createDrag(this, "Potion", 4, 45);
    private void onPotionElementsRender(final MatrixStack matrixStack, final EventRender renderEvent) {
        float width = 57.0F;
        float height = 21.0F;
        float x = this.HUDPotion.getX();
        float y = this.HUDPotion.getY();
        StyledFont small = Fonts.newcode[12];
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

        int index = 0;
        this.CounterEffects = 0.0F;
        for(Iterator var8 = mc.player.getActivePotionEffects().iterator(); var8.hasNext(); ++index) {
            EffectInstance effectInstance = (EffectInstance)var8.next();
            String durationText = EffectUtils.getPotionDurationString(effectInstance, 1.0F);
            String text = I18n.format(effectInstance.getEffectName(), new Object[0]);
            int levelpotion = effectInstance.getAmplifier() + 1;
            float textWidth = 50.0F;
            float textWidth2 = 50.0F;
            float textWidth3 = 50.0F;
            boolean isNegative = !effectInstance.getPotion().isBeneficial();
            Color effectColor = isNegative ? new Color(218, 7, 7, 255) : new Color(255, 255, 255, 255);

            if (effectInstance.getAmplifier() != 0) {
                textWidth = small.getWidth(text + " " + levelpotion + effectInstance.getAmplifier() + TextFormatting.GRAY + " [" + TextFormatting.RED + durationText + TextFormatting.GRAY + "]") + 11.0F;
                textWidth2 = small.getWidth(text + " " + levelpotion + TextFormatting.GRAY + " [");
                textWidth3 = small.getWidth(text + " " + levelpotion + TextFormatting.GRAY + " [" + durationText);
            }

            if (effectInstance.getAmplifier() == 0) {
                textWidth = small.getWidth(text + " " + levelpotion + TextFormatting.GRAY + " [" + TextFormatting.RED + durationText + TextFormatting.GRAY + "]") + 11.0F;
                textWidth2 = small.getWidth(text + TextFormatting.GRAY + " [");
                textWidth3 = small.getWidth(text + TextFormatting.GRAY + " [" + durationText);
            }
            if (x < mc.getMainWindow().scaledWidth() / 2 - 23) {
                float finalTextWidth = textWidth;
                float finalY = y;
                IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x, finalY, finalTextWidth, 10F + 3, 3, ColorUtils.rgba(20, 20, 20, 200)));
                RenderUtils.Render2D.drawRoundedRect(x, y, textWidth, 10F + 3, 3, ColorUtils.rgba(20, 20, 20, 200));

                if (effectInstance.getAmplifier() != 0) {
                    small.drawString(matrixStack, text + " " + levelpotion + TextFormatting.GRAY + " ", (double) (x + 12.0F), (double) (y + 5.5F), effectColor.getRGB());
                    small.drawString(matrixStack, ClientUtils.gradient(durationText, firstColor2, firstColor2), (double) (x + 12.0F + textWidth2), (double) (y + 5.5F), (new Color(255, 255, 255, 255)).getRGB());
                    small.drawString(matrixStack, "" + TextFormatting.GRAY + "", (double) (x + 12.0F + textWidth3), (double) (y + 5.5F), effectColor.getRGB());
                }

                if (effectInstance.getAmplifier() == 0) {
                    small.drawString(matrixStack, text + TextFormatting.GRAY + " ", (double) (x + 12.0F), (double) (y + 5.5F), effectColor.getRGB());
                    small.drawString(matrixStack, ClientUtils.gradient(durationText, firstColor2, firstColor2), (double) (x + 12.0F + textWidth2), (double) (y + 5.5F), (new Color(255, 255, 255, 255)).getRGB());
                    small.drawString(matrixStack, "" + TextFormatting.GRAY + "", (double) (x + 12.0F + textWidth3), (double) (y + 5.5F), effectColor.getRGB());
                }

                PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
                Effect effect = effectInstance.getPotion();
                TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
                mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
                DisplayEffectsScreen.blit(matrixStack, (int) (x + 2F), (int) y + 3, 10, 8, 8, textureatlassprite);
            } else {
                float finalTextWidth1 = textWidth;
                float finalY1 = y;
                IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(x - finalTextWidth1 + 57, finalY1, finalTextWidth1, 10F + 3, 3, ColorUtils.rgba(20, 20, 20, 200)));
                RenderUtils.Render2D.drawRoundedRect(x - textWidth + 57, y, textWidth, 10F + 3, 3, ColorUtils.rgba(20, 20, 20, 200));

                if (effectInstance.getAmplifier() != 0) {
                    small.drawString(matrixStack, text + " " + levelpotion + TextFormatting.GRAY + " ", (double) (x + 12.0F - textWidth) + 57, (double) (y + 5.5F), effectColor.getRGB());
                    small.drawString(matrixStack, ClientUtils.gradient(durationText, firstColor2, firstColor2), (double) (x + 12.0F + textWidth2 - textWidth) + 57, (double) (y + 5.5F), (new Color(255, 255, 255, 255)).getRGB());
                    small.drawString(matrixStack, "" + TextFormatting.GRAY + "", (double) (x + 12.0F + textWidth3 - textWidth) + 57, (double) (y + 5.5F), effectColor.getRGB());
                }

                if (effectInstance.getAmplifier() == 0) {
                    small.drawString(matrixStack, text + TextFormatting.GRAY + " ", (double) (x + 12.0F - textWidth) + 57, (double) (y + 5.5F), effectColor.getRGB());
                    small.drawString(matrixStack, ClientUtils.gradient(durationText, firstColor2, firstColor2), (double) (x + 12.0F + textWidth2 - textWidth) + 57, (double) (y + 5.5F), (new Color(255, 255, 255, 255)).getRGB());
                    small.drawString(matrixStack, "" + TextFormatting.GRAY + "", (double) (x + 12.0F + textWidth3 - textWidth) + 57, (double) (y + 5.5F), effectColor.getRGB());
                }

                PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
                Effect effect = effectInstance.getPotion();
                TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
                mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
                DisplayEffectsScreen.blit(matrixStack, (int) (x + 2.2F - textWidth) + 57, (int) y + 3, 10, 8, 8, textureatlassprite);
            }
            y += 14F;
        }
        this.CounterEffects = (float)index;
        this.HUDPotion.setWidth(width);
        this.HUDPotion.setHeight(height * this.CounterEffects + 18.0F);
    }

    float health = 0;
    float healthplus = 0;

    public final Dragging targetHUD = NewCode.createDrag(this, "TargetHUD", 500, 50);
    private final Animation targetHudAnimation = new EaseBackIn(200, 1, 1.5f);
    private LivingEntity target = null;
    private double scale = 0.0D;
    private void onRenderTargetHUD(final MatrixStack stack) {
        this.target = getTarget();
        this.targetHudAnimation.setDuration(150);
        this.scale = targetHudAnimation.getOutput();
        int firstColor2 = (mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
        int secondColor2 = (mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());
        if (scale == 0.0F) {
            target = null;
        }

        if (target == null) {
            return;
        }

        if (scale == 0.0F) {
            target = null;
        }

        if (target == null) {
            return;
        }
        boolean isInvisible = target instanceof LivingEntity && ((LivingEntity) target).isInvisible();

        updatePlayerHealth();

        final String targetName = this.target.getName().getString();
        String substring = targetName.substring(0, Math.min(targetName.length(), 10));
        float xPosition = targetHUD.getX() - 5, yPosition = targetHUD.getY() - 4, maxWidth = 100, maxHeight = 48;

        this.health = AnimationMath.fast(health, target.getHealth() / target.getMaxHealth(), 5);
        this.health = MathHelper.clamp(this.health, 0, 1);
        this.healthplus = AnimationMath.fast(this.healthplus, target.getAbsorptionAmount() / target.getMaxHealth(), 5);
        this.healthplus = MathHelper.clamp(this.healthplus, 0, 1);

        GlStateManager.pushMatrix();
        AnimationMath.sizeAnimation(xPosition + (maxWidth / 2), yPosition + (maxHeight / 2), scale);
        IWrapper.blurQueue.add(() -> RenderUtils.Render2D.drawRoundedRect(xPosition + 5, yPosition + 4, maxWidth - 18, maxHeight - 13, 5, ColorUtils.rgba(20, 20, 20, 200)));
        RenderUtils.Render2D.drawRoundedRect(xPosition + 5, yPosition + 4, maxWidth - 18, maxHeight - 13, 5, ColorUtils.rgba(20, 20, 20, 200));


        RenderUtils.Render2D.drawGradientRound(xPosition + 9F, yPosition + 31.0F, 74.5F, 3.5F, 1.5F, ColorUtils.rgba(19, 21, 23, 255), ColorUtils.rgba(19, 21, 23, 255), ColorUtils.rgba(19, 21, 23, 255), ColorUtils.rgba(19, 21, 23, 255));
        RenderUtils.Render2D.drawGradientRound(xPosition + 9F, yPosition + 31.0F, 74.5F * this.health, 3.5F, 1.5F, firstColor2, firstColor2, secondColor2, secondColor2);

        if (!ClientUtils.isConnectedToServer("funtime")) {
            RenderUtils.Render2D.drawGradientRound(xPosition + 9F, yPosition + 31.0F, 74.5F * this.healthplus - 0.7f, 3.5F, 1.5F, (new Color(255, 89, 0, 255)).getRGB(), (new Color(255, 255, 0, 255)).getRGB(), (new Color(255, 89, 0, 255)).getRGB(), (new Color(255, 255, 0, 255)).getRGB());
        }
        if (target instanceof AbstractClientPlayerEntity) {
            RenderUtils.Render2D.drawRoundFace(xPosition + 8, yPosition + 7, 20, 20, 6f, 255, (AbstractClientPlayerEntity) target);
        } else if (target instanceof MobEntity) {
            StencilUtils.initStencilToWrite();
            RenderUtils.Render2D.drawRoundedRect(xPosition + 8, yPosition + 7, 20, 20, 6f, rgba(21, 21, 21, 190));
            StencilUtils.readStencilBuffer(1);
            RenderUtils.Render2D.drawRoundedRect(xPosition + 8, yPosition + 7, 20, 20, 6f, new Color(23, 23, 23, 50).getRGB());
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/other/vopros.png"), xPosition + 8, yPosition + 7, 20, 20, new Color(255, 255, 255, 255).getRGB());
            StencilUtils.uninitStencilBuffer();
        }

        drawItemStack(xPosition + 30f, yPosition + 24 - 6f, 9f);

        SmartScissor.push();
        SmartScissor.setFromComponentCoordinates(xPosition + 20, yPosition, 65, 40);

        Fonts.msMedium[15].drawString(stack, substring, xPosition + 30, yPosition + 11, -1);


        SmartScissor.unset();
        SmartScissor.pop();

        String healthValue = (int) MathUtil.round(this.health * 100, 1f) + "%";
        float healthWid = Fonts.msMedium[11].getWidth(healthValue);
        float healthWid2 = Fonts.msMedium[11].getWidth("Неизвестно");
        if (isInvisible && ClientUtils.isConnectedToServer("funtime")) {
            Fonts.msMedium[11].drawCenteredString(stack, "Неизвестно", (double) (xPosition + 9F + 57.25f - (healthWid2 / 2)), (double) (yPosition + 32.0F), -1);
        } else {
            Fonts.msMedium[11].drawString(stack, healthValue, (double) (xPosition + 9F + 37.25f - (healthWid / 2)), (double) (yPosition + 32.0F), -1);
        }

        boolean hasPlayerHead = target.getHeldItemMainhand().getItem() == Items.PLAYER_HEAD ||
                target.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
        if (target instanceof AbstractClientPlayerEntity) {
            if (mc != null && mc.player != null) {
                if (Manager.FUNCTION_MANAGER.autoMessage.state) {
                    String formattedTargetName = targetName != null ? formatTargetName(targetName) : "null";
                    if (Manager.FUNCTION_MANAGER.autoMessage.shar.get() && hasPlayerHead) {
                        if (timerUtil.hasTimeElapsed(21500) && target != null && target.getHealth() <= 6) {
                            String[] messages = {
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball/elytra, I’ll let go or I’ll beat your balls right now" : " кидай шар/элитры, отпускаю или я тебе щас яйца бить буду"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball/elytra, I'll let you go or I'll bite your balls off right now" : " кидай шар/элитры, отпускаю или я тебе щас яйца откушу"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball, otherwise I'll blow you away now" : " кидай шар/элитры, иначе я тебя сейчас тряхну"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball/elytra, you have 3 seconds left" : " кидай шар/элитры, даю 5 секунд потом яйца кусаю"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball, don't test my patience" : " кидай шар/элитры, пока я тебе хууём по носу не дал"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball, don't test my patience" : " кидай шар/элитры, пока я тебе хууём в нос не дал"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры или я на тебя кота натравлю"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры или на колени на колени"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры пока я твоей маме не рассказал что ты уроки не сделал"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры или я тебя на дик посажу"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры пока я залупой по зубам твойм не прошёлся"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры пока на дик ртом не упал"),
                                    formattedTargetName + (Translated.isRussian() ? " throw the ball or I'll set my cat on you" : " кидай шар/элитры или я на тебя кота натравлю")
                            };

                            String randomMessage = messages[new Random().nextInt(messages.length)];
                            mc.player.sendChatMessage(randomMessage);
                            timerUtil.reset();
                        }
                    }

                    if (Manager.FUNCTION_MANAGER.autoMessage.kill.get()) {
                        if (timerUtil.hasTimeElapsed(550) && target != null && target.getHealth() <= 0.0f) {
                            mc.player.sendChatMessage("!-" + formattedTargetName);
                            timerUtil.reset();
                        }
                    }
                }
            }
        }

        GlStateManager.popMatrix();
        this.targetHUD.setWidth(maxWidth - 18);
        this.targetHUD.setHeight(maxHeight - 13);
    }

    private static String formatTargetName(String name) {
        if (name == null || name.isEmpty()) return "null";
        long upperCaseCount = name.chars().filter(Character::isUpperCase).count();
        if ((double) upperCaseCount / name.length() > 0.5) {
            return name.toLowerCase();
        }
        return name;
    }

    private void updatePlayerHealth() {
        String myPlayerName = mc.player.getName().getString();

        if (target.getName().getString().equals(myPlayerName)) {
            return;
        }

        if (ClientUtils.isConnectedToServer("space-times") || ClientUtils.isConnectedToServer("reallyworld") || ClientUtils.isConnectedToServer("funtime") || ClientUtils.isConnectedToServer("legendsgrief")) {
            for (Map.Entry<ScoreObjective, Score> entry : IMinecraft.mc.world.getScoreboard().getObjectivesForEntity(target.getName().getString()).entrySet()) {
                Score score = entry.getValue();
                int newHealth = score.getScorePoints();

                if (newHealth >= 1) {
                    target.setHealth(newHealth);
                } else {
                    target.setHealth(1);
                }
            }
        }
    }


    private void drawItemStack(float x, float y, float offset) {
        List<ItemStack> stackList = new ArrayList<>(Arrays.asList(target.getHeldItemMainhand(), target.getHeldItemOffhand()));
        List<ItemStack> armorItems = (List<ItemStack>) target.getArmorInventoryList();
        stackList.add(armorItems.get(3));
        stackList.add(armorItems.get(2));
        stackList.add(armorItems.get(1));
        stackList.add(armorItems.get(0));
        final AtomicReference<Float> posX = new AtomicReference<>(x);
        stackList.stream()
                .filter(stack -> !stack.isEmpty())
                .forEach(stack -> HudUtil.drawItemStack(stack,
                        posX.getAndAccumulate(offset, Float::sum),
                        y,
                        true,
                        true, 0.53f));
    }

    private LivingEntity getTarget() {
        LivingEntity target = null;

        if (Manager.FUNCTION_MANAGER.auraFunction.getTarget() instanceof LivingEntity) {
            target = (LivingEntity) Manager.FUNCTION_MANAGER.auraFunction.getTarget();
            targetHudAnimation.setDirection(Direction.FORWARDS);
        } else if (IMinecraft.mc.currentScreen instanceof ChatScreen) {
            target = IMinecraft.mc.player;
            targetHudAnimation.setDirection(Direction.FORWARDS);
        } else {
            targetHudAnimation.setDirection(Direction.BACKWARDS);

        }
        return target;
    }
}
