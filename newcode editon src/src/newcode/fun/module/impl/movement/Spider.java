package newcode.fun.module.impl.movement;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.math.RayTraceUtil;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "Spider", type = TypeList.Movement)
public class Spider extends Module {
    TimerUtil timerUtil = new TimerUtil();
    public ModeSetting mode = new ModeSetting("Режим", "Грим", "Грим", "Матрикс", "Шар");
    private final SliderSetting spiderSpeed = new SliderSetting("Скорость", 2.0f, 1.0f, 10.0f, 0.05f).setVisible(() -> mode.is("Матрикс"));

    public Spider() {
        addSettings(mode, spiderSpeed);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventMotion) {
            EventMotion e = (EventMotion) event;
            handleEventMotion(e);
        }
        return false;
    }

    private void handleEventMotion(EventMotion motion) {
        if (!mc.player.collidedHorizontally) {
            return;
        }

        switch (mode.get()) {
            case "Матрикс": {
                long speed = MathHelper.clamp(500 - (spiderSpeed.getValue().longValue() / 2 * 100), 0, 500);
                if (timerUtil.hasTimeElapsed(speed)) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.collidedVertically = true;
                    mc.player.collidedHorizontally = true;
                    mc.player.isAirBorne = true;
                    mc.player.jump();
                    timerUtil.reset();
                }
                break;
            }
            case "Грим": {
                handleGrimMode(motion);
                break;
            }
            case "Шар": {
                handleSphereMode(motion);
                break;
            }
        }
    }

    private void handleGrimMode(EventMotion motion) {
        if (mc.player.isOnGround()) {
            motion.setOnGround(true);
            mc.player.setOnGround(true);
            mc.player.jump();
        }
        if (mc.player.fallDistance > 0 && mc.player.fallDistance < 2) {
            placeBlockFromHotbar(motion);
        }
    }

    private void handleSphereMode(EventMotion motion) {
        if (mc.player.isOnGround()) {
            motion.setOnGround(true);
            mc.player.setOnGround(true);
            mc.player.jump();
        }
        if (mc.player.fallDistance > 0 && mc.player.fallDistance < 2) {
            ItemStack offHandItem = mc.player.getHeldItemOffhand();
            if (offHandItem.getItem() == Items.PLAYER_HEAD) {
                motion.setPitch(80);
                motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());

                BlockRayTraceResult r = (BlockRayTraceResult) RayTraceUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
                mc.player.swingArm(Hand.OFF_HAND);
                mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.OFF_HAND, r);
                mc.player.fallDistance = 0;
            } else {
                motion.setPitch(80);
                motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());

                BlockRayTraceResult r = (BlockRayTraceResult) RayTraceUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
                mc.player.swingArm(Hand.OFF_HAND);
                mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.OFF_HAND, r);
                mc.player.fallDistance = 0;
            }
        }
    }

    private void placeBlockFromHotbar(EventMotion motion) {
        int block = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() instanceof BlockItem) {
                block = i;
                break;
            }
        }

        if (block == -1) {
            ClientUtils.sendMessage("Для использования этого спайдера у вас должны блоки в хотбаре!");
            toggle();
            return;
        }

        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        motion.setPitch(80);
        motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());

        BlockRayTraceResult r = (BlockRayTraceResult) RayTraceUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
        mc.player.swingArm(Hand.MAIN_HAND);
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, r);
        mc.player.inventory.currentItem = last;
        mc.player.fallDistance = 0;
    }
}
