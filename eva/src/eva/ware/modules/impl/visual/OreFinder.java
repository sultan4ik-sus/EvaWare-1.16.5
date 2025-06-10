package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.*;
import eva.ware.manager.Theme;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.font.Fonts;
import net.minecraft.block.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@ModuleRegister(name = "OreFinder", category = Category.Visual)
public class OreFinder extends Module {
    CopyOnWriteArrayList<BlockPos> waiting = new CopyOnWriteArrayList<>();
    public ModeSetting mode = new ModeSetting("Тип", "Bypass", "Default", "Bypass");

    public SliderSetting up = new SliderSetting("Вверх", 5, 0, 30, 1).visibleIf(() -> mode.is("Bypass"));

    public SliderSetting down = new SliderSetting("Вниз", 5, 0, 30, 1).visibleIf(() -> mode.is("Bypass"));
    public SliderSetting radius = new SliderSetting("Радиус", 20, 0, 30, 1).visibleIf(() -> mode.is("Bypass"));
    public SliderSetting delay = new SliderSetting("Задержка", 13, 0, 40, 1).visibleIf(() -> mode.is("Bypass"));
    public SliderSetting skip = new SliderSetting("Пропуск", 3, 1, 5, 1).visibleIf(() -> mode.is("Bypass"));


    public ModeListSetting ores = new ModeListSetting("Искать",
            new CheckBoxSetting("Уголь", false),
            new CheckBoxSetting("Железо", false),
            new CheckBoxSetting("Редстоун", false),
            new CheckBoxSetting("Золото", false),
            new CheckBoxSetting("Эмеральды", false),
            new CheckBoxSetting("Алмазы", false),
            new CheckBoxSetting("Незерит", false),
            new CheckBoxSetting("Лазурит", false)
    );

    public OreFinder() {
        addSettings(mode, radius, up, down, delay, skip, ores);
    }

    CopyOnWriteArrayList<BlockPos> clicked = new CopyOnWriteArrayList<>();
    TimerUtility timerUtility = new TimerUtility();
    BlockPos clicking;
    Thread thread;

    @Override
    public void onDisable() {
        super.onDisable();
        if (thread != null) {
            thread.interrupt();
            thread.stop();
        }
        clicking = null;
        clicked.clear();
        waiting.clear();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mode.is("Bypass")) {
            waiting = removeEveryOther(getBlocks(), skip.getValue().intValue());
            thread = new Thread(() -> {
                if (mc.player != null) {
                    for (BlockPos click : waiting) {
                        mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, click, Direction.UP));
                        clicked.add(click);
                        clicking = click;
                    }
                }
            });
            thread.start();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Default")) {
            if (timerUtility.isReached(2000)) {
                waiting = removeEveryOther(getBlocks(), 1);
                thread = new Thread(() -> {
                    if (mc.player != null) {
                        for (BlockPos click : waiting) {
                            clicked.add(click);
                            clicking = click;
                        }
                    }
                });
                thread.start();
                timerUtility.reset();
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (thread != null) {
            if (thread.isAlive()) {
                if (e.isSend()) {
                    if (mode.is("Bypass")) {
                        if (e.getPacket() instanceof CPlayerPacket) {
                            e.cancel();
                        }
                        if (e.getPacket() instanceof CAnimateHandPacket) {
                            e.cancel();
                        }
                        if (e.getPacket() instanceof CPlayerTryUseItemPacket) {
                            e.cancel();
                        }
                        if (e.getPacket() instanceof CHeldItemChangePacket) {
                            e.cancel();
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (thread != null) {
            if (thread.isAlive()) {
                e.cancel();
            }
        }
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mode.is("Bypass")) {
            long millis = ((long) waiting.size() * delay.getValue().intValue()) - clicked.size() * delay.getValue().intValue();
            String time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            float clickedWidth = Fonts.montserrat.getWidth(clicked.size() + "/" + waiting.size(), 8);
            float timeWidth = Fonts.montserrat.getWidth(time, 8);
            float width = clickedWidth + timeWidth + 20;
            float heigth = 15;

            float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2;
            RenderUtility.drawShadowFancyRect(mc.getMainWindow().getScaledWidth() / 2f - width / 2, 10, width, heigth);
            Fonts.montserrat.drawText(e.getMatrixStack(), clicked.size() + "/" + waiting.size(), x + 5, 13, Theme.textColor, 8);
            Fonts.montserrat.drawText(e.getMatrixStack(), time, x + width - Fonts.montserrat.getWidth(time, 8) - 5, 13, Theme.textColor, 8);
        }
    }

    @Subscribe
    public void onWorld(EventRender3D e) {
        if (clicked != null && clicking != null) {
            RenderUtils.drawBlockBox(clicking, ColorUtility.rgba(83, 252, 154, 255));
            for (BlockPos click : clicked) {
                int color = isValid(click);
                if (color != -1) {
                    RenderUtils.drawBlockBox(click, color);
                }
            }
        }
    }


    private static <T> CopyOnWriteArrayList<T> removeEveryOther(CopyOnWriteArrayList<T> inList, int offset) {
        if (offset == 1) return inList;
        CopyOnWriteArrayList<T> outList = new CopyOnWriteArrayList<>();
        @SuppressWarnings("unchecked")
        T[] ts = (T[]) inList.toArray();
        for (int i = 0; i < ts.length; i++) {
            if (i % offset == 0) {
                outList.add(ts[i]);
            }
        }
        return outList;
    }

    CopyOnWriteArrayList<BlockPos> getBlocks() {
        CopyOnWriteArrayList<BlockPos> blocks = new CopyOnWriteArrayList<>();
        BlockPos start = mc.player.getPosition();
        int dis = (mode.is("Bypass") ? radius.getValue().intValue() : 25);
        int up = (mode.is("Bypass") ? this.up.getValue().intValue() : 25);
        int down = (mode.is("Bypass") ? this.down.getValue().intValue() : 25);
        for (int y = up; y >= -down; y--)
            for (int x = dis; x >= -dis; x--)

                for (int z = dis; z >= -dis; z--) {
                    BlockPos pos = start.add(x, y, z);

                    if (pos.getY() > 0) {
                        Block block = mc.world.getBlockState(pos).getBlock();
                        if (mode.is("Bypass")) {
                            if (block instanceof AirBlock) continue;
                        }
                        if (mode.is("Default")) {
                            if (!(block == Blocks.COAL_ORE) && ores.is("Уголь").getValue()
                                    || !(block == Blocks.DIAMOND_ORE) && ores.is("Алмазы").getValue()
                                    || !(block == Blocks.EMERALD_ORE) && ores.is("Эмеральды").getValue()
                                    || (block == Blocks.GOLD_ORE) && ores.is("Золото").getValue()
                                    || (block == Blocks.REDSTONE_ORE) && ores.is("Редстоун").getValue()
                                    || (block == Blocks.ANCIENT_DEBRIS) && ores.is("Незерит").getValue()
                                    || (block == Blocks.IRON_ORE) && ores.is("Железо").getValue())
                                continue;
                        }
                        blocks.add(pos);
                    }

                }
        return blocks;
    }

    private int isValid(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof OreBlock ore) {
            if (ore == Blocks.COAL_ORE && ores.get(0).getValue()) {
                return ColorUtility.rgba(12, 12, 12, 255);
            }
            if (ore == Blocks.IRON_ORE && ores.get(1).getValue()) {
                return ColorUtility.rgba(122, 122, 122, 255);
            }
            if (ore == Blocks.REDSTONE_ORE && ores.get(2).getValue()) {
                return ColorUtility.rgba(255, 82, 82, 255);
            }
            if (ore == Blocks.GOLD_ORE && ores.get(3).getValue()) {
                return ColorUtility.rgba(247, 255, 102, 255);
            }
            if (ore == Blocks.EMERALD_ORE && ores.get(4).getValue()) {
                return ColorUtility.rgba(116, 252, 101, 255);
            }
            if (ore == Blocks.DIAMOND_ORE && ores.get(5).getValue()) {
                return ColorUtility.rgba(77, 219, 255, 255);
            }
            if (ore == Blocks.ANCIENT_DEBRIS && ores.get(6).getValue()) {
                return ColorUtility.rgba(105, 60, 12, 255);
            }
            if (ore == Blocks.LAPIS_ORE && ores.get(7).getValue()) {
                return ColorUtility.rgba(41, 41, 255, 255);
            }
        }
        return -1;
    }
}
