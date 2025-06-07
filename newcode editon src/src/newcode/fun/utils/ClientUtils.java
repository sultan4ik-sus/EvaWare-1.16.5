package newcode.fun.utils;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.jagrosh.discordipc.entities.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import newcode.fun.control.Manager;
import newcode.fun.utils.render.Vec2i;
import org.lwjgl.glfw.GLFW;
import newcode.fun.utils.math.KeyMappings;
import newcode.fun.utils.render.ColorUtils;
import wtf.wither.Api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientUtils implements IMinecraft {

    public static User me;

    private static final String discordID = "1330055843282026536";
    private static final DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    private static final DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    public static ServerData serverData;
    private static boolean pvpMode;
    private static UUID uuid;

    public static String getKey(int integer) {
        if (integer < 0) {
            return switch (integer) {
                case -100 -> I18n.format("key.mouse.left");
                case -99 -> I18n.format("key.mouse.right");
                case -98 -> I18n.format("key.mouse.middle");
                default -> "MOUSE" + (integer + 101);
            };
        } else {
            String keyName = GLFW.glfwGetKeyName(integer, -1);
            if (keyName == null) {
                keyName = KeyMappings.reverseKeyMap.get(integer);
            }
            if (keyName != null) {
                keyName = replaceCyrillicWithLatin(keyName);
            }
            return keyName != null ? keyName : "UNKNOWN";
        }
    }

    /**
     * Заменяет русские буквы на соответствующие английские буквы (эмуляция переключения раскладки).
     *
     * @param input Строка для обработки.
     * @return Строка с заменёнными русскими символами.
     */
    private static String replaceCyrillicWithLatin(String input) {
        Map<Character, Character> cyrillicToLatinMap = new HashMap<>();
        String cyrillic = "йцукенгшщзхъфывапролджэячсмитьбю";
        String latin = "qwertyuiop[]asdfghjkl;'zxcvbnm,.";

        for (int i = 0; i < cyrillic.length(); i++) {
            cyrillicToLatinMap.put(cyrillic.charAt(i), latin.charAt(i));
            cyrillicToLatinMap.put(Character.toUpperCase(cyrillic.charAt(i)), Character.toUpperCase(latin.charAt(i)));
        }

        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            result.append(cyrillicToLatinMap.getOrDefault(ch, ch));
        }
        return result.toString();
    }


    public static void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize(discordID, eventHandlers, true, null);
        ClientUtils.discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        ClientUtils.discordRichPresence.largeImageKey = "https://s11.gifyu.com/images/S1u9R.gif";
        ClientUtils.discordRichPresence.largeImageText = "https://newcode.fun";
        new Thread(() -> {
            while (true) {
                try {
                    ClientUtils.discordRichPresence.details = "Name » " + Api.getName();
                    ClientUtils.discordRichPresence.state = "UID » " + Api.getUID();
                    discordRPC.Discord_UpdatePresence(discordRichPresence);
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    public static void updateBossInfo(SUpdateBossInfoPacket packet) {
        if (packet.getOperation() == SUpdateBossInfoPacket.Operation.ADD) {
            if (StringUtils.stripControlCodes(packet.getName().getString()).toLowerCase().contains("pvp")) {
                pvpMode = true;
                uuid = packet.getUniqueId();
            }
        } else if (packet.getOperation() == SUpdateBossInfoPacket.Operation.REMOVE) {
            if (packet.getUniqueId().equals(uuid))
                pvpMode = false;
        }
    }

    public static boolean isPvP() {
        return pvpMode;
    }

    public static boolean isConnectedToServer(String ip) {
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
            return mc.getCurrentServerData().serverIP.toLowerCase().contains(ip.toLowerCase());
        }
        return false;
    }

    public static Vec2i getMouse(int mouseX, int mouseY) {
        return new Vec2i((int) (mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2), (int) (mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2));
    }

    public static void sendMessage(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(gradient("NewCode", new java.awt.Color(ColorUtils.getColorStyle(180)).getRGB(), new java.awt.Color(ColorUtils.getColorStyle(360)).getRGB()).append(new StringTextComponent(TextFormatting.DARK_GRAY + " » " + TextFormatting.RESET + message)), Util.DUMMY_UUID);
    }

    public static StringTextComponent gradient(String message, int first, int end) {

        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(ColorUtils.interpolateColor(first, end, (float) i / message.length())))));
        }

        return text;

    }

    public static ITextComponent replace(ITextComponent original, String find, String replaceWith) {
        if (original == null || find == null || replaceWith == null) {
            return original;
        }

        String originalText = original.getString();
        String replacedText = originalText.replace(find, replaceWith);
        return new StringTextComponent(replacedText);
    }
}
