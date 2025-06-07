package newcode.fun.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.Manager;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.math.Rotation;
import newcode.fun.utils.misc.TimerUtil;

import java.util.*;

import static net.minecraft.client.Minecraft.player;

@Annotation(name = "ChestStealer", type = TypeList.Player)
public class ChestStealer extends Module {

    boolean doClick = false;

    private final BooleanOption chestClose = new BooleanOption("Закрывать пустой", false);
    private final BooleanOption toggle = new BooleanOption("Выкл когда фул инв", false);
    private final SliderSetting stealDelay = new SliderSetting("Задержка лутания", 1, 0, 20, 1);
    public Vector2f rotate = new Vector2f(0, 0);


    private final BooleanOption miss = new BooleanOption("Промахиваться", false);
    private final SliderSetting missPercent = new SliderSetting("Шанс на промах", 50, 0, 100, 1).setVisible(() -> miss.get());

    private final TimerUtil timerUtil = new TimerUtil();

    public ChestStealer() {
        addSettings(chestClose, toggle, stealDelay, miss, missPercent);
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
            if (mc.player.inventory.mainInventory.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onEvent(final Event event) {
            if (event instanceof EventUpdate) {
                if (mc.player.openContainer instanceof ChestContainer) {
                    ChestContainer container = (ChestContainer) mc.player.openContainer;
                    for (int r = 0; r < container.inventorySlots.size() / 1.3; ++r) {
                        IInventory p = container.getLowerChestInventory();
                        int index = new Random().nextInt(0, container.inventorySlots.size());
                        if (index <= container.inventorySlots.size()) {
                            if (timerUtil.hasTimeElapsed(stealDelay.getValue().longValue() - 1)) {
                                if (p.getStackInSlot(index).getItem() != Item.getItemById(0)) {
                                    if (miss.get()) {
                                        if (new Random().nextInt(0, 100) >= missPercent.getValue().intValue()) {
                                            doClick = true;
                                        } else {
                                            doClick = false;
                                            timerUtil.reset();
                                            continue;
                                        }
                                    } else {
                                        doClick = true;
                                    }
                                    if (doClick) {
                                        mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                                    } else {
                                        timerUtil.reset();
                                        continue;
                                    }
                                    timerUtil.reset();
                                    continue;
                                } else {
                                    timerUtil.reset();
                                    continue;
                                }
                            }
                            if (toggle.get() && isInventoryFull()) {
                                mc.player.closeScreen();
                            }
                            if (container.getLowerChestInventory().isEmpty() && chestClose.get()) {
                                mc.player.closeScreen();
                            }
                        }
                    }
                }
            }
        return false;
    }
}
