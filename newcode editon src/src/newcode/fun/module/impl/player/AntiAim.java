package newcode.fun.module.impl.player;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventMotion;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.move.MoveUtil;

@Annotation(
        name = "AntiAim",
        type = TypeList.Player, desc = "Крутит вас в указную сторону"
)
public class AntiAim extends Module {
    public boolean AA;
    public Vector2f rotate = new Vector2f(0.0F, 0.0F);

    private final BooleanOption rotationOption1 = (new BooleanOption("Pitch", true));
    private final BooleanOption visualRotationOption = (new BooleanOption("Визуальная", true));
    private float yaw;
    private float pitch;
    private final BooleanOption rotationOption = (new BooleanOption("Yaw", true));

    private final SliderSetting rotationSpeedSetting = new SliderSetting("Скорость Yaw", 1.0F, 0.1F, 150.0F, 0.1F).setVisible(() -> rotationOption.get());
    private final SliderSetting rotationSpeedSetting1 = new SliderSetting("Кастомный Pitch", 1.0F, -90.0F, 90.0F, 0.1F).setVisible(() -> rotationOption1.get());

    public AntiAim() {
        this.yaw = 0.0F;
        this.addSettings(new Setting[]{this.rotationSpeedSetting, this.rotationSpeedSetting1,  this.rotationOption, this.rotationOption1, this.visualRotationOption});
    }
    private long started;
    @Override
    protected void onEnable() {
        super.onEnable();
        started = System.currentTimeMillis();
        lastPos = mc.player.getPositionVec();
    }

    float animation;

    Vector3d lastPos = new Vector3d(0, 0, 0);
    public boolean onEvent(Event event) {
       if (!visualRotationOption.get()) {
           if (!MoveUtil.isMoving() || !mc.player.isElytraFlying()) {
               if (event instanceof EventMotion) {
                   this.AA = false;
                   EventMotion eventMotion1 = (EventMotion) event;
                   float pitch;
                   if (this.rotationOption.get()) {
                       pitch = (Float) this.rotationSpeedSetting.getValue();
                       if (pitch > 0.0F) {
                           this.yaw += pitch;
                           if (this.yaw >= 360.0F) {
                               this.yaw = 0.0F;
                           }

                           if (!this.visualRotationOption.get()) {
                               eventMotion1.setYaw(this.yaw);
                           }

                           mc.player.rotationYawHead = this.yaw;
                           mc.player.renderYawOffset = this.yaw;
                       } else {
                           this.yaw = 0.0F;
                           if (!this.visualRotationOption.get()) {
                               eventMotion1.setYaw(this.yaw);
                           }

                           mc.player.rotationYawHead = mc.player.renderYawOffset = mc.player.rotationYaw = this.yaw;
                       }
                   }

                   if (this.rotationOption1.get()) {
                       pitch = (Float) this.rotationSpeedSetting1.getValue();
                       if (!this.visualRotationOption.get()) {
                           eventMotion1.setPitch(pitch);
                       }

                       mc.player.rotationPitchHead = pitch;
                       this.AA = false;
                   }
               }

           }
       } else {
               if (event instanceof EventMotion) {
                   this.AA = false;
                   EventMotion eventMotion1 = (EventMotion) event;
                   float pitch;
                   if (this.rotationOption.get()) {
                       pitch = (Float) this.rotationSpeedSetting.getValue();
                       if (pitch > 0.0F) {
                           this.yaw += pitch;
                           if (this.yaw >= 360.0F) {
                               this.yaw = 0.0F;
                           }

                           if (!this.visualRotationOption.get()) {
                               eventMotion1.setYaw(this.yaw);
                           }

                           mc.player.rotationYawHead = this.yaw;
                           mc.player.renderYawOffset = this.yaw;
                       } else {
                           this.yaw = 0.0F;
                           if (!this.visualRotationOption.get()) {
                               eventMotion1.setYaw(this.yaw);
                           }

                           mc.player.rotationYawHead = mc.player.renderYawOffset = mc.player.rotationYaw = this.yaw;
                       }
                   }

                   if (this.rotationOption1.get()) {
                       pitch = (Float) this.rotationSpeedSetting1.getValue();
                       if (!this.visualRotationOption.get()) {
                           eventMotion1.setPitch(pitch);
                       }

                       mc.player.rotationPitchHead = pitch;
                       this.AA = false;
                   }
               }


       }
        return false;
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

}

