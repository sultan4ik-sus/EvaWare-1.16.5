package eva.ware.modules.impl.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.MoveUtility;
import eva.ware.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AirItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;

@ModuleRegister(name = "WaterSpeed", category = Category.Movement)
public class WaterSpeed extends Module {
	
	private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Matrix", "Grim", "Funtime");
	private SliderSetting speed = new SliderSetting("Скорость", 4f, 1f, 10f, 0.1f).visibleIf(() -> mode.is("Grim"));
	private StrafeMovement strafeMovement = new StrafeMovement();
    private CheckBoxSetting smartWork = new CheckBoxSetting("Умный", true).visibleIf(() -> mode.is("Matrix"));

	int tick;

	public WaterSpeed() {
		addSettings(mode, speed, smartWork);
	}
	
	@Subscribe
	public void onPlayer(EventMoving e) {
		if (mode.is("Grim")) {
			if (mc.player.isInWater() && MoveUtility.isMoving()) {
                if (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motion.y = 0;
                }

                mc.player.setSwimming(true);

                float moveSpeed = speed.getValue() + new Random().nextFloat() * 1.1f;
                moveSpeed /= 100.0f;

                double moveX = mc.player.getForward().x * moveSpeed;
                double moveZ = mc.player.getForward().z * moveSpeed;

                mc.player.addVelocity(moveX, 0, moveZ);
            }
		}
	}
	
	@Subscribe
	public void onUpdate(EventUpdate e) {
        if (mode.is("Funtime")) {
            if (mc.player != null && mc.player.isAlive()) {
                if (mc.player.isInWater()) {
                    mc.player.setMotion(mc.player.getMotion().x * (double) 1.0505, mc.player.getMotion().y, mc.player.getMotion().z * (double) 1.0505);
                }
            }
        }

		if (mode.is("Matrix")) {
            List<ItemStack> stacks = new ArrayList<>();
            mc.player.getArmorInventoryList().forEach(stacks::add);
            stacks.removeIf(w -> w.getItem() instanceof AirItem);
            float motion = (float) MoveUtility.getMotion();
            boolean hasEnchantments = false;
            for (ItemStack stack : stacks) {

                int enchantmentLevel = 0;

                if (buildEnchantments(stack, 1)) {
                    enchantmentLevel = 1;
                }

                if (enchantmentLevel > 0) {
                    motion = 0.5f;
                    hasEnchantments = true;
                }
            }

			if (mc.player.collidedHorizontally) {
                tick = 0;
                return;
            }
            if (!mc.player.isInWater()) return;
            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isSneaking() && !(mc.world.getBlockState(mc.player.getPosition().add(0, 1, 0)).getBlock() instanceof AirBlock)) {
                mc.player.motion.y = 0.12f;
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -0.35f;
            }

            if (smartWork.getValue()) {
                if (!mc.player.isPotionActive(Effects.SPEED)) {
                    tick = 0;
                    return;
                }

                if (!hasEnchantments) {
                    return;
                }
            }

            if (mc.world.getBlockState(mc.player.getPosition().add(0, 1, 0)).getBlock() instanceof AirBlock && mc.gameSettings.keyBindJump.isKeyDown()) {
                tick++;
                mc.player.motion.y = 0.12f;
            }
            tick++;
            MoveUtility.setMotion(0.4f);
            strafeMovement.setOldSpeed(0.4);
        }
    }

    public boolean buildEnchantments(ItemStack stack, float strenght) {
        if (stack != null) {
            if (stack.getItem() instanceof ArmorItem) {
                return EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack) > 0;
            }
        } else {
            return false;
        }

        return false;
	}
}
