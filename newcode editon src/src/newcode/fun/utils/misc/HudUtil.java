package newcode.fun.utils.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import newcode.fun.module.impl.combat.AttackAura;
import org.lwjgl.opengl.GL11;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.font.styled.StyledFont;

import java.util.List;

import static newcode.fun.utils.render.RenderUtils.Render2D.drawTexture;

public class HudUtil implements IMinecraft {

    public static float calculateBPS() {
        double distance = Math.sqrt(Math.pow(mc.player.getPosX() - mc.player.prevPosX, 2) +
                Math.pow(mc.player.getPosY() - mc.player.prevPosY, 2) +
                Math.pow(mc.player.getPosZ() - mc.player.prevPosZ, 2));
        float bps = (float) (distance * mc.timer.timerSpeed * 20.0D);
        return (float) (Math.round(bps * 10) / 10.0f);
    }

    public static float calculateBPST() {
        if (Manager.FUNCTION_MANAGER.auraFunction.target != null) {
            double distance = Math.sqrt(Math.pow(Manager.FUNCTION_MANAGER.auraFunction.target.getPosX() - Manager.FUNCTION_MANAGER.auraFunction.target.prevPosX, 2) +
                    Math.pow(Manager.FUNCTION_MANAGER.auraFunction.target.getPosY() - Manager.FUNCTION_MANAGER.auraFunction.target.prevPosY, 2) +
                    Math.pow(Manager.FUNCTION_MANAGER.auraFunction.target.getPosZ() - Manager.FUNCTION_MANAGER.auraFunction.target.prevPosZ, 2));
            float bps = (float) (distance * mc.timer.timerSpeed * 20.0D);
            return (float) (Math.round(bps * 10) / 10.0f);
        } else {
            float bps = 0;
        }
        return 0;
    }

    public static void drawItemStack(ItemStack stack, float x, float y, boolean withoutOverlay, boolean scale, float scaleValue) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        if (scale) GL11.glScaled(scaleValue, scaleValue, scaleValue);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withoutOverlay) mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
        RenderSystem.popMatrix();
    }

    public static int calculatePing() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null ?
                mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }

    public static String serverIP() {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && !mc.isSingleplayer() ? mc.getCurrentServerData().serverIP : "";
    }

    public static List<Module> getSorted(StyledFont font) {
        List<Module> modules = Manager.FUNCTION_MANAGER.getFunctions();
        modules.sort((o1, o2) -> {
            float width1 = font.getWidth(o1.name) + 4;
            float width2 = font.getWidth(o2.name) + 4;
            return Float.compare(width2, width1);
        });
        return modules;
    }

    public static void drawFace(float x, float y, float width, float height, float radius, AbstractClientPlayerEntity target) {
        ResourceLocation skin = target.getLocationSkin();
        mc.getTextureManager().bindTexture(skin);
        drawTexture(x, y, width, height, radius, 1F);
    }

}
