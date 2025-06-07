package newcode.fun.control.cmd.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.language.Translated;
import newcode.fun.utils.render.ColorUtils;
import newcode.fun.utils.render.RenderUtils;

import static newcode.fun.utils.render.RenderUtils.Render2D.drawShadow;

@CmdInfo(
        name = "gps", description = "Прокладывает путь до координат", descriptionen = "Paves the way to the coordinates")
public class GPSCmd extends Cmd {
    public static boolean enabled;
    public static Vector3d vector3d;

    public GPSCmd() {
    }

    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("off")) {
                ClientUtils.sendMessage(Translated.isRussian() ? "Navigator is turned off" : "Навигатор выключен");
                enabled = false;
                vector3d = null;
                return;
            }
            int positionX = mc.player.getPosition().getX();
            int positionZ = mc.player.getPosition().getZ();
            if (args[1].equalsIgnoreCase("death")) {
                int x = Integer.parseInt(String.valueOf(positionX));
                int y = Integer.parseInt(String.valueOf(positionZ));
                enabled = true;
                vector3d = new Vector3d((double)x, 0.0, (double)y);
                ClientUtils.sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Navigator is on! Coordinates " + x + ";" + y : TextFormatting.GRAY + "Навигатор включен! Координаты " + x + ";" + y);
            }

            if (args.length == 3) {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                enabled = true;
                vector3d = new Vector3d((double)x, 0.0, (double)y);
                ClientUtils.sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Navigator is on! Coordinates " + x + ";" + y : TextFormatting.GRAY + "Навигатор включен! Координаты " + x + ";" + y);
            }
        } else {
            this.error();
        }

    }
    private boolean isPlayerDead() {
        return mc.player.getHealth() < 1.0f && mc.currentScreen instanceof DeathScreen;
    }

    public static void drawArrow(MatrixStack stack) {
        if (enabled) {
            double x = vector3d.x - IMinecraft.mc.getRenderManager().info.getProjectedView().getX();
            double z = vector3d.z - IMinecraft.mc.getRenderManager().info.getProjectedView().getZ();
            Minecraft var10000 = mc;
            double cos = (double)MathHelper.cos((double)mc.player.rotationYaw * 0.017453292519943295);
            var10000 = mc;
            double sin = (double)MathHelper.sin((double)mc.player.rotationYaw * 0.017453292519943295);
            double rotY = -(z * cos - x * sin);
            double rotX = -(x * cos + z * sin);
            float angle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI);
            double x2 = (double)(14 * MathHelper.cos(Math.toRadians((double)angle)) + (float)IMinecraft.mc.getMainWindow().getScaledWidth() / 2.0F);
            double y2 = (double)(14 * MathHelper.sin(Math.toRadians((double)angle)) + (float)IMinecraft.mc.getMainWindow().getScaledHeight() / 4.3F);
            GlStateManager.pushMatrix();
            GlStateManager.disableBlend();
            GlStateManager.translated(x2, y2, 0.0);
            GlStateManager.rotatef(angle, 0.0F, 0.0F, 1.0F);
            Minecraft var10001 = IMinecraft.mc;
            double var21 = Math.pow(vector3d.x - mc.player.getPosX(), 2.0);
            Minecraft var10002 = IMinecraft.mc;
            double dst = Math.sqrt(var21 + Math.pow(vector3d.z - mc.player.getPosZ(), 2.0));
            int clr = ColorUtils.rgba(255, 255, 255, 255);
            RenderUtils.Render2D.drawImage(new ResourceLocation("newcode/images/all/arrow/arrow.png"), -4, -2F, 16F, 16F, ColorUtils.setAlpha(clr, 150));
            GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            Fonts.newcode[13].drawCenteredStringWithOutline(stack, (int)dst + "m", 5.5, 6, -1);
            GlStateManager.enableBlend();
            GlStateManager.popMatrix();
        }
    }

    public void error() {
        this.sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Error in use" + TextFormatting.WHITE + ":" : TextFormatting.GRAY + "Ошибка в использовании" + TextFormatting.WHITE + ":");
        this.sendMessage(TextFormatting.WHITE + ".gps " + TextFormatting.GRAY + "<" + TextFormatting.RED + "x, z" + TextFormatting.GRAY + ">");
        this.sendMessage(TextFormatting.WHITE + ".gps " + TextFormatting.GRAY + "<" + TextFormatting.RED + "death" + TextFormatting.GRAY + ">");
        this.sendMessage(TextFormatting.WHITE + ".gps " + TextFormatting.GRAY + "<" + TextFormatting.RED + "off" + TextFormatting.GRAY + ">");
    }
}
