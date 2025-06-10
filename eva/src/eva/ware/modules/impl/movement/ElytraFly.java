package eva.ware.modules.impl.movement;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventUpdate;
import eva.ware.events.EventMoving;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.player.InventoryUtility;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import static eva.ware.utils.player.InventoryUtility.getHotbarSlotOfItem;

@ModuleRegister(name = "ElytraFly", category = Category.Movement)
public class ElytraFly extends Module {
	private final TimerUtility timerUtility = new TimerUtility();
	private final TimerUtility timerUtility1 = new TimerUtility();
    public static long lastStartFalling;

    public ModeSetting mode = new ModeSetting("Мод", "Matrix", "Matrix", "Matrix Glide", "Firework");

    private SliderSetting motionY = new SliderSetting("Скорость Y", 0.2f, 0.1f, 0.5f, 0.01f).visibleIf(() -> mode.is("Matrix"));
    private SliderSetting motionX = new SliderSetting("Скорость XZ", 1.2f, 0.1f, 5f, 0.1f).visibleIf(() -> !mode.is("Firework"));
	private CheckBoxSetting autojump = new CheckBoxSetting("Авто прыжок", false).visibleIf(() -> mode.is("Matrix"));
	private CheckBoxSetting saveMe = new CheckBoxSetting("Спасать", false).visibleIf(() -> mode.is("Matrix"));
	private final SliderSetting timerStartFireWork = new SliderSetting("Таймер фейерверка", 400, 50, 1500, 10).visibleIf(() -> mode.is("Firework"));
	private final CheckBoxSetting onlyGrimBypass = new CheckBoxSetting("Обход Grim", true).visibleIf(() -> mode.is("Firework"));
	boolean launchRocket = true;
	public ElytraFly() {
    	addSettings(mode, motionX, motionY, autojump, saveMe, timerStartFireWork, onlyGrimBypass);
    }
    
	@Subscribe
	public void onMoving(EventMoving e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		if (mode.is("Matrix Glide")) {
			int elytra = InventoryUtility.getSlotIDFromItem(Items.ELYTRA);
			if (elytra == -1) {
				return;
			}
			Vector3d motion = e.motion;
			if (System.currentTimeMillis() - lastStartFalling > 1000) {
				disabler(elytra);
			}
			if (System.currentTimeMillis() - lastStartFalling < 800 && !mc.player.isSneaking()) {
				motion.y = motionY.getValue().doubleValue();
			} else {
				motion.y -= 0.05;
			}
			mc.player.jump();
			mc.player.motion.y = motion.y;
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		timerUtility.reset();
		timerUtility1.reset();
	}
    
    @Subscribe
    public void onUpdate(EventUpdate e) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		if (mode.is("Firework")) {

			long elytraSwapTime = 550;

			if (onlyGrimBypass.getValue()) {
				elytraSwapTime = 0;
				if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
					launchRocket = false;
				} else {
					launchRocket = true;
				}
			} else {
				launchRocket = true;
			}

	        for (int i = 0; i < 9; i++) {
				if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.01, mc.player.getPosZ())).getBlock() == Blocks.AIR && !mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isElytraFlying()) {
					if (timerUtility1.isReached(elytraSwapTime)) {
						mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
						mc.player.startFallFlying();
						mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
						mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
						timerUtility1.reset();
					}

					if (launchRocket) {
						if (mc.player.isElytraFlying()) {
							if (timerUtility.isReached(timerStartFireWork.getValue().longValue())) {
								InventoryUtility.inventorySwapClick(Items.FIREWORK_ROCKET, false);
								timerUtility.reset();
							}
						}
					}
	            }
	        }
		}

    	if (mode.is("Matrix")) {
			int elytra = getHotbarSlotOfItem();
			if (MoveUtility.reason(false)) {
				return;
			}
			if (elytra == -1) {
				return;
			}
			if (mc.player.onGround) {
				if (autojump.getValue()) {
					mc.player.jump();
				}
				timerUtility.reset();
			} else if (timerUtility.isReached(350)) {

				if (mc.player.ticksExisted % 2 == 0) {
					disabler(elytra);

				}

				mc.player.motion.y = mc.player.ticksExisted % 2 != 0 ? -0.25 : 0.25;

				if (saveMe.getValue()) {
					if ((!MoveUtility.isBlockUnder(4f) || mc.player.collidedHorizontally || mc.player.collidedVertically)) {
						mc.player.motion.y = motionY.getValue();
					}
				}

				if (!mc.player.isSneaking() && mc.gameSettings.keyBindJump.pressed) {
					mc.player.motion.y = motionY.getValue();
				}

				if (mc.gameSettings.keyBindSneak.isKeyDown()) {
					mc.player.motion.y = -motionY.getValue();
				}

				MoveUtility.setMotion(motionX.getValue());
			}

    	}
    }
    
	public static void disabler(int elytra) {
		if (elytra != -2) {
			mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
		}
		mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, Action.START_FALL_FLYING));
		mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, Action.START_FALL_FLYING));
		if (elytra != -2) {
			mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
		}
		lastStartFalling = System.currentTimeMillis();
	}

}
