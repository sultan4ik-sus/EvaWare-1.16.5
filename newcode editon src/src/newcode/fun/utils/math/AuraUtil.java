package newcode.fun.utils.math;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import newcode.fun.utils.IMinecraft;

import static net.minecraft.util.math.MathHelper.clamp;

public class AuraUtil implements IMinecraft {

    public static Vector3d getVector(LivingEntity target) {
        final double wHalf = target.getWidth() / 2;

        final double yExpand = clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());
        final double xExpand = clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        final double zExpand = clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);

        final double yOffset = target.getPosYEye() > mc.player.getPosYEye() ? -1 : 0;

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand + yOffset,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }

}
