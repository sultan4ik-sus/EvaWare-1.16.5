package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventMotion;
import eva.ware.events.EventPacket;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.ui.notify.impl.WarningNotify;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@ModuleRegister(name = "ElytraSpeed", category = Category.Movement)
public class ElytraSpeed extends Module {
	private final TimerUtility timerUtility = new TimerUtility();
	private final ModeSetting mode = new ModeSetting("Тип", "Grim", "Grim", "ReallyWorld");
	private final SliderSetting boostSpeed = new SliderSetting("Cкорость буста", 0.3F, 0.0F, 0.8F, 1.0E-4F);
	private final CheckBoxSetting safeMode = new CheckBoxSetting("Безопасный режим", true);
	private final CheckBoxSetting autoJump = new CheckBoxSetting("Авто прыжок", false);
	int oldItem = -1;
	public ElytraSpeed() {
		addSettings(mode, boostSpeed, safeMode, autoJump);
	}

	@Subscribe
	public void onPacket(EventPacket e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		if (InventoryUtility.getInstance().getSlotInInventory(Items.ELYTRA) == -1 || InventoryUtility.getInstance().getSlotInInventory(Items.FIREWORK_ROCKET) == -1) {
			return;
		}

		if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
			return;
		}
		if (e.getPacket() instanceof SEntityMetadataPacket && ((SEntityMetadataPacket)e.getPacket()).getEntityId() == mc.player.getEntityId()) {
			e.cancel();
		}
	}
	
	@Subscribe
	public void onUpdate(EventUpdate e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
			return;
		}

		if (InventoryUtility.getInstance().getSlotInInventory(Items.ELYTRA) == -1 || InventoryUtility.getInstance().getSlotInInventory(Items.FIREWORK_ROCKET) == -1) {
			return;
		}

		if (safeMode.getValue()) {
			if (mc.player.collidedHorizontally) {
				print(getName() + ": " + TextFormatting.RED + "Вы столкнулись с блоком!");
				Evaware.getInst().getNotifyManager().add(0, new WarningNotify("Вы столкнулись с блоком!", 1000));
				toggle();
				return;
			}
		}

		mc.gameSettings.keyBindBack.setPressed(false);
		mc.gameSettings.keyBindLeft.setPressed(false);
		mc.gameSettings.keyBindRight.setPressed(false);

		if (this.autoJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround()) {
			mc.gameSettings.keyBindJump.setPressed(true);
		}
		int timeSwap = 600;
		if (mode.is("Grim")) {
			timeSwap = 200;
		}
		if (mc.player.isElytraFlying()) {
			mc.gameSettings.keyBindSneak.setPressed(true);
		} else {
			mc.gameSettings.keyBindSneak.setPressed(false);
		}

		if (InventoryUtility.getItemSlot(Items.FIREWORK_ROCKET) == -1 || mc.player.collidedHorizontally || !InventoryUtility.doesHotbarHaveItem(Items.ELYTRA)) {
			return;
		}

		if (this.autoJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
			mc.player.jump();
		}

		if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
			mc.playerController.onStoppedUsingItem(mc.player);
		}

		for (int i = 0; i < 9; ++i) {
			if (mc.player.inventory.getStackInSlot(i).getItem() != Items.ELYTRA || !(mc.player.fallDistance < 4.0f) || mc.player.isOnGround() || mc.player.isInWater() || mc.player.isInLava() || mc.player.collidedHorizontally || !timerUtility.hasTimeElapsed2(timeSwap)) continue;
			mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
			mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
			for (Entity entity : mc.world.getAllEntities()) {
				if (!(entity instanceof FireworkRocketEntity) || !(mc.player.getDistance(entity) < 4.0f) || entity.ticksExisted >= 30) continue;
				float speed = 0.9f + boostSpeed.getValue();
				MoveUtility.setMotion(speed);
			}
			mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
			this.oldItem = i;
			if (!timerUtility.hasTimeElapsed2(timeSwap)) continue;
			InventoryUtility.inventorySwapClick(Items.FIREWORK_ROCKET, false);
			timerUtility.reset();
		}
	}

	@Subscribe
	public void onMotion(EventMotion e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		if (InventoryUtility.getInstance().getSlotInInventory(Items.ELYTRA) == -1 || InventoryUtility.getInstance().getSlotInInventory(Items.FIREWORK_ROCKET) == -1) {
			return;
		}

		if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
			return;
		}
		if (!Evaware.getInst().getModuleManager().getHitAura().isEnabled() || Evaware.getInst().getModuleManager().getHitAura().getTarget() == null) {
			mc.player.rotationPitchHead = 15;
			e.setPitch(15);
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		if (oldItem != -1) {
			if (mc.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA) {
				mc.playerController.windowClick(0, oldItem < 9 ? oldItem + 36 : oldItem, 38, ClickType.SWAP, mc.player);
			}
			oldItem = -1;
		}

		timerUtility.reset();
	}
}