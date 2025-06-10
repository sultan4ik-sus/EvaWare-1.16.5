package eva.ware.modules.impl.movement;

import eva.ware.Evaware;
import net.minecraft.block.FlowingFluidBlock;
import com.google.common.eventbus.Subscribe;

import eva.ware.events.*;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.MoveUtility;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;

@ModuleRegister(name = "Jesus", category = Category.Movement)
public class Jesus extends Module {
	
	private ModeSetting mode = new ModeSetting("Мод", "Matrix Solid", "Matrix Solid", "Matrix Zoom", "AAC", "NCP", "Meta","NCP New");
	private SliderSetting speed = new SliderSetting("Скорость", 10f, 0.1f, 10f, 0.1f).visibleIf(() -> !mode.is("NCP") && !mode.is("AAC") && !mode.is("NCP New"));
	private CheckBoxSetting noJump = new CheckBoxSetting("Не прыгать", false).visibleIf(() -> mode.is("Matrix Solid"));
	private CheckBoxSetting bypassboolean = new CheckBoxSetting("Новый Matrix", true).visibleIf(() -> mode.is("Matrix Zoom"));
	private int ticks;
	public static boolean jesusTick;
	public static boolean swap;
	private final float melonBallSpeed = 0.44F;
	
	public Jesus() {
		addSettings(mode, speed, noJump, bypassboolean);
	}
	
    @Subscribe
    private void onUpdate(EventMotion motion) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

		double x = mc.player.getPosX();
		double y = mc.player.getPosY();
		double z = mc.player.getPosZ();
    	if (mode.is("Matrix Solid")) {
            updateMoveInWater();
            updateAirMove(motion);
    	}
		if (mode.is("Meta")){
			if (mc.player != null) {
				if (mc.player.isInWater() || mc.player.isInLava()) {
					EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
					EffectInstance DeEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);
					ItemStack offHandItem = mc.player.getHeldItemOffhand();
					String itemName = offHandItem.getDisplayName().getString();
					float appliedSpeed = 0F;

					if (itemName.contains("Ломтик Дыни") && speedEffect != null && speedEffect.getAmplifier() == 2) {
						appliedSpeed = 0.4283F * 1.15F;
					}
					else {
						if (speedEffect != null) {
							if (speedEffect.getAmplifier() == 2) {
								appliedSpeed = melonBallSpeed * 1.15F;
							}
							else if (speedEffect.getAmplifier() == 1) {
								appliedSpeed = melonBallSpeed;
							}
						}
						else {
							appliedSpeed = melonBallSpeed * 0.68F;
						}
					}

					if (DeEffect != null) {
						appliedSpeed *= 0.85f;
					}

					MoveUtility.setSpeed(appliedSpeed);

					boolean isMoving = mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown()
							|| mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();
					if (!isMoving) {
						mc.player.motion.x = 0.0;
						mc.player.motion.z = 0.0;
					}
					mc.player.motion.y = mc.gameSettings.keyBindJump.isKeyDown() ? 0.019 : 0.003;
				}
			}
		}

		if (mode.is("NCP New")) {
			if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - (mc.player.isJumping ? 0.01 : 0.45), mc.player.getPosZ())).getBlock() == Blocks.WATER && !mc.player.isInWater()) {
				motion.setOnGround(false);
				if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.001, mc.player.getPosZ())).getBlock() == Blocks.WATER && mc.player.isJumping) {
					mc.player.motion.y = 0.41999998688697815;
				}

				mc.player.onGround = false;
				mc.player.motion.x = 0.0;
				mc.player.motion.z = 0.0;
				if (!mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = 0.2865F;
				}

				if (mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = 0.4005F;
				}
			}
		}
		
		if (mode.is("NCP")) {
			if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER) {
				mc.player.motion.y = 0.0391;
				mc.player.onGround = false;

				if (!mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = mc.player.isMoving() ? 0.2865F : 0.294F;
				}

				if (mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = 0.41F;
				}

				if (mc.player.collidedHorizontally && mc.gameSettings.keyBindForward.isKeyDown() && !mc.player.isInWater() && !mc.player.isInLava() && mc.gameSettings.keyBindJump.isKeyDown()) {
					mc.player.jump();
				}
			}

			if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA) {
				mc.player.motion.y = 0.04;
				mc.player.onGround = false;

				if (!mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = 0.2865F;
				}

				if (mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jumpMovementFactor = 0.4005F;
				}

				if (mc.player.collidedHorizontally && mc.gameSettings.keyBindForward.isKeyDown() && !mc.player.isInWater() && !mc.player.isInLava() && mc.gameSettings.keyBindJump.isKeyDown()) {
					mc.player.jump();
				}
			}

			if (mc.player.isInWater() || mc.player.isInLava()) {
				mc.player.motion.x = 0.0;
				mc.player.motion.z = 0.0;
				if (!mc.gameSettings.keyBindJump.isKeyDown()) {
					mc.player.motion.y += 0.07;
				}
			}
		}

		if (mode.is("AAC")) {
			if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - (mc.player.isJumping ? 0.01 : 0.45), mc.player.getPosZ())).getBlock() == Blocks.WATER && !mc.player.isInWater()) {
				motion.setOnGround(false);
				if (mc.player.onGround && mc.player.ticksExisted % 2 == 0) {
					mc.player.isAirBorne = motion.isOnGround();
				}
			}
		}
    	
    	if (mode.is("Matrix Zoom")) {
			if (swap) {
				if (bypassboolean.getValue()) {
					motion.setY(motion.getY() + (ticks % 3 == 0 ? -0.02 : ticks % 3 == 1 ? 0.02 : 0.03));
				} else {
					motion.setY(motion.getY() + (ticks % 2 == 0 ? -0.02 : 0.02));
				}
				ticks++;
				if (motion.getY() % 1 == 0) {
					motion.setY(motion.getY() - 0.02);
				}
				motion.setOnGround(false);
			}
			swap = false;
    	}
    }
    
    @Subscribe
    private void onMove(EventMoving move) {
		if (Evaware.getInst().getModuleManager().getFreeCam().isEnabled()) return;

    	if (mode.is("Matrix Zoom")) {
			BlockPos pos = new BlockPos(move.getFrom());
			Block block = mc.world.getBlockState(pos).getBlock();
    		if (block instanceof FlowingFluidBlock) {
				Strafe.waterTicks = 10;
				move.motion.z = 0;
				move.motion.x = 0;
    		} else if (mc.world.getBlockState(new BlockPos(move.to())).getBlock() instanceof FlowingFluidBlock) {
				Strafe.waterTicks = 10;
				boolean bypass = false;
				try {
					bypass = bypassboolean.getValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				float f;
				MoveUtility.setSpeed(f = ((ticks) % (bypass ? 3 : 2) == 0 ? speed.getValue() - 0.01f : 0.14f));
				if (mc.player.getPosY() % 1 == 0) {
					move.motion.y= 0;
				}
				jesusTick = true;
				swap = true;
				move.motion.x = mc.player.motion.x;
				move.motion.z = mc.player.motion.z;
				move.collisionOffset().y = -0.7;
				mc.player.motion.y = 0;
				mc.player.motion.x = 0;
				mc.player.motion.z = 0;
    		}
    	}
    }
    
    private void updateMoveInWater() {
        BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.008D, mc.player.getPosZ());
        Block playerBlock = mc.world.getBlockState(playerPos).getBlock();
        if (playerBlock == Blocks.WATER && !mc.player.isOnGround()) {
            boolean isUp = mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.03D, mc.player.getPosZ())).getBlock() == Blocks.WATER;
            mc.player.jumpMovementFactor = 0.0F;
            float yPort = MoveUtility.getMotion() > 0.1D ? 0.02F : 0.032F;
            mc.player.setVelocity(mc.player.motion.x *= speed.getValue(), (double) mc.player.fallDistance < 3.5D ? (double) (isUp ? yPort : -yPort) : -0.1D, mc.player.motion.z *= speed.getValue());
        }
    }

    private void updateAirMove(EventMotion motion) {
        double posY = mc.player.getPosY();
        if (posY > (double) ((int) posY) + 0.89D && posY <= (double) ((int) posY + 1) || (double) mc.player.fallDistance > 3.5D) {
            mc.player.getPositionVec().y = ((double) ((int) posY + 1) + 1.0E-45D);
            if (!mc.player.isInWater()) {
                BlockPos waterBlockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.1D, mc.player.getPosZ());
                Block waterBlock = mc.world.getBlockState(waterBlockPos).getBlock();
                if (waterBlock == Blocks.WATER) {
                    moveInWater(motion);
                }
            }
        }
    }
    
    private void moveInWater(EventMotion motion) {
        motion.setOnGround(true);
        collisionJump();
        if (ticks == 1) {
            MoveUtility.setMotion(1.1f);
            ticks = 0;
        } else {
            ticks = 1;
        }
    }

	private void collisionJump() {
		if (mc.player.collidedHorizontally && (!noJump.getValue() || mc.gameSettings.keyBindJump.pressed)) {
			mc.player.motion.y = 0.2D;
			mc.player.motion.x = 0.0D;
			mc.player.motion.z = 0.0D;
		}
	}

	private void adjustPlayerPosition() {
		if (mc.player.getPosY() < (int) mc.player.getPosY() + 0.01D) {
			mc.player.setPosition(mc.player.getPosX(), (int) mc.player.getPosY() + 0.01D, mc.player.getPosZ());
		}
	}

	private void handleFall() {
		if (mc.player.getMotion().y < -0.5) {
			mc.player.setVelocity(mc.player.getMotion().x, 0.0D, mc.player.getMotion().z);
		}
		mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
		mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
	}
}