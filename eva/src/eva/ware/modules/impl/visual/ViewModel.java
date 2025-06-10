package eva.ware.modules.impl.visual;

import com.mojang.blaze3d.matrix.MatrixStack;

import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.impl.combat.HitAura;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


@ModuleRegister(name = "ViewModel", category = Category.Visual)
public class ViewModel extends Module {

    public CheckBoxSetting swingAnim = new CheckBoxSetting("SwingAnim", true);
    public ModeSetting animationMode = new ModeSetting("Мод", "Smooth", "Back", "Smooth Down", "Block", "Smooth", "Swipe Back", "Block Down").visibleIf(() -> swingAnim.getValue());
    public SliderSetting swingPower = new SliderSetting("Сила", 5, 1.0f, 10.0f, 0.05f).visibleIf(() -> swingAnim.getValue());
    public SliderSetting swingSpeed = new SliderSetting("Скорость", 8, 3.0f, 10.0f, 1.0f).visibleIf(() -> swingAnim.getValue());
    public SliderSetting scale = new SliderSetting("Размер", 1, 0.5f, 1.5f, 0.05f).visibleIf(() -> swingAnim.getValue());
    public final CheckBoxSetting onlyAura = new CheckBoxSetting("Только с HitAura", false).visibleIf(() -> swingAnim.getValue());

    public final SliderSetting x = new SliderSetting("Позиция X", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting y = new SliderSetting("Позиция Y", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting z = new SliderSetting("Позиция Z", 0.0F, -2.0f, 2.0f, 0.1F);

    public HitAura hitAura;

    public ViewModel(HitAura hitAura) {
        this.hitAura = hitAura;
        addSettings(swingAnim, animationMode, swingPower, swingSpeed, scale, onlyAura, x, y, z);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);
        float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

        if (onlyAura.getValue() && (hitAura.getTarget() == null)) {
            return;
        }

        switch (animationMode.getValue()) {
            case "Swipe Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.1);
                stack.rotate(Vector3f.YP.rotationDegrees(60));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                stack.rotate(Vector3f.XP.rotationDegrees(-10 - (swingPower.getValue() * 10) * anim));
                stack.rotate(Vector3f.XP.rotationDegrees(-60));
            }

            case "Block Down" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.1);
                stack.translate(0.5, -0.1, 0);
                stack.rotate(Vector3f.XP.rotationDegrees(sin2 * -45));
                stack.translate(-0.5, 0.1, 0);

                stack.translate(0.5, -0.1, 0);
                stack.rotate(Vector3f.YP.rotationDegrees(sin2 * -20));
                stack.translate(-0.5, 0.1, 0);

                stack.rotate(Vector3f.YP.rotationDegrees(50));
                stack.rotate(Vector3f.XP.rotationDegrees(-90));
                stack.rotate(Vector3f.YP.rotationDegrees(50));
            }
            
            case "Back" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
            }
            case "Smooth Down" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0, 0, 0);
                stack.rotate(Vector3f.YP.rotationDegrees(15 * anim));

                stack.rotate(Vector3f.ZP.rotationDegrees(-60 * anim));
                stack.rotate(Vector3f.XP.rotationDegrees((-90 - (swingPower.getValue())) * anim));
            }
            case "Block" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.4f, 0, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-30));
                stack.rotate(Vector3f.XP.rotationDegrees(-90 - (swingPower.getValue() * 10) * anim));
            }
            default -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                runnable.run();
            }
        }
    }

}
