package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.AttackEvent;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.*;
import eva.ware.ui.clienthud.impl.*;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.render.color.ColorUtility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "Hud", category = Category.Visual)
public class Hud extends Module {

    public final ModeListSetting elements = new ModeListSetting("Элементы",
            new CheckBoxSetting("Ватермарка", true),
            new CheckBoxSetting("Информация", true),
            new CheckBoxSetting("Лист эффектов", true),
            new CheckBoxSetting("Лист модеров", true),
            new CheckBoxSetting("Лист биндов", true),
            new CheckBoxSetting("Лист событий (ReallyWorld)", false),
            new CheckBoxSetting("Таргет худ", true),
            new CheckBoxSetting("Таймер", false),
            new CheckBoxSetting("Уведомления", true),
            new CheckBoxSetting("Броня", true)
    );

    public final ModeSetting rectMode = new ModeSetting("Вид ректов", "Обычный", "Обычный", "Простой");
    public final ModeSetting waterMarkMode = new ModeSetting("Вид ватермарки", "Обычный", "Обычный", "Плитка", "Время", "Табличка").visibleIf(() -> elements.is("Ватермарка").getValue());
    public final ModeSetting tHudMode = new ModeSetting("Вид таргет худа", "Обычный", "Обычный", "Кругляк").visibleIf(() -> elements.is("Таргет худ").getValue());
    public final CheckBoxSetting particlesOnTarget = new CheckBoxSetting("Партиклы", true).visibleIf(() -> elements.is("Таргет худ").getValue());
    public final ModeListSetting waterMarkOptions = new ModeListSetting("Отображать в вт", new CheckBoxSetting("Пользователь", true), new CheckBoxSetting("Фпс", true), new CheckBoxSetting("Пинг", true), new CheckBoxSetting("Сервер", true)).visibleIf(() -> elements.is("Ватермарка").getValue() && waterMarkMode.is("Обычный"));
    public final ModeListSetting infoOptions = new ModeListSetting("Отображать в инфо", new CheckBoxSetting("Координаты", true), new CheckBoxSetting("Скорость", true), new CheckBoxSetting("ТПС", true)).visibleIf(() -> elements.is("Информация").getValue());

    public static final ModeSetting themeMode = new ModeSetting("Палитра темы", "Шаблон", "Шаблон", "Кастом");

    public static final ColorSetting themeColor = new ColorSetting("Цвет темы", new Color(134, 80, 150).getRGB()).visibleIf(() -> themeMode.is("Кастом"));

    public static final ModeSetting theme = new ModeSetting("Тема", "Синий",
            "Синий", "Красный", "Фиолетовый", "Розовый", "Зеленый", "Эстетичный", "Бирюзовый", "Темный"
    ).visibleIf(() -> themeMode.is("Шаблон"));

    final Watermark watermark;
    final ClientInfo clientInfo;
    final PotionHud potionHud;
    final TimerHud timerHud;
    final Keybinds keybinds;
    final TargetHud targetHud;
    final ArmorHud armorHud;
    final StaffHud staffHud;
    final Schedules schedules;

    @Getter
    private final CopyOnWriteArrayList<TargetHud.HeadParticle> particles = new CopyOnWriteArrayList();

    @Subscribe
    private void onAttack(AttackEvent e) {
        if (elements.is("Таргет худ").getValue() && particlesOnTarget.getValue()) {
            targetHud.onAttack(e);
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (elements.is("Лист модеров").getValue()) staffHud.update(e);
        if (elements.is("Таймер").getValue()) timerHud.update(e);
        if (elements.is("Лист событий (ReallyWorld)").getValue()) schedules.update(e);
    }


    @Subscribe
    private void onDisplay(EventRender2D e) {
        if (mc.gameSettings.showDebugInfo || e.getType() != EventRender2D.Type.POST) {
            return;
        }

        if (elements.is("Информация").getValue()) clientInfo.render(e);
        if (elements.is("Лист событий (ReallyWorld)").getValue()) schedules.render(e);
        if (elements.is("Лист эффектов").getValue()) potionHud.render(e);
        if (elements.is("Лист биндов").getValue()) keybinds.render(e);
        if (elements.is("Лист модеров").getValue()) staffHud.render(e);
        if (elements.is("Таргет худ").getValue()) targetHud.render(e);
        if (elements.is("Ватермарка").getValue()) watermark.render(e);
        if (elements.is("Броня").getValue()) armorHud.render(e);
        if (elements.is("Таймер").getValue()) timerHud.render(e);
    }

    public Hud() {
        watermark = new Watermark();
        clientInfo = new ClientInfo();
        Dragging potions = Evaware.getInst().createDrag(this, "Potions", 278, 5);
        armorHud = new ArmorHud();
        Dragging timerInfo = Evaware.getInst().createDrag(this, "Timer", 300, 20);
        Dragging keyBinds = Evaware.getInst().createDrag(this, "KeyBinds", 185, 5);
        Dragging dragging = Evaware.getInst().createDrag(this, "TargetHUD", 74, 128);
        Dragging staffList = Evaware.getInst().createDrag(this, "StaffList", 96, 5);
        Dragging schedules = Evaware.getInst().createDrag(this, "Schedules", 165, 5);
        potionHud = new PotionHud(potions);
        keybinds = new Keybinds(keyBinds);
        staffHud = new StaffHud(staffList);
        targetHud = new TargetHud(dragging);
        timerHud = new TimerHud(timerInfo);
        this.schedules = new Schedules(schedules);
        addSettings(elements, rectMode, waterMarkMode, tHudMode, particlesOnTarget, waterMarkOptions, infoOptions, themeMode, theme, themeColor);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtility.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
}