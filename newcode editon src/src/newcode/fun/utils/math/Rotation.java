package newcode.fun.utils.math;

import net.minecraft.client.Minecraft;

public class Rotation {
    public static float packedYaw = Integer.MIN_VALUE, packetPitch = Integer.MIN_VALUE;
    private static final Minecraft mc = Minecraft.getInstance();

    public static float getRealYaw() {
        if (mc.player == null) return -1;

        return packedYaw == Integer.MIN_VALUE ? mc.player.rotationYaw : packedYaw;
    }

    public static float getRealPitch() {
        if (mc.player == null) return -1;

        return packetPitch == Integer.MIN_VALUE ? mc.player.rotationPitch : packetPitch;
    }
}

