package eva.ware.utils.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import eva.ware.ui.mainmenu.AltScreen;
import eva.ware.utils.math.Vector2i;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.StringUtils;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import eva.ware.Evaware;
import eva.ware.ui.mainmenu.MainScreen;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.util.UUID;

@UtilityClass
public class ClientUtility implements IMinecraft {
    public static String getHWID() {
        String hwid = System.getProperty("os.name") +
                System.getProperty("user.name") +
                Runtime.getRuntime().availableProcessors() +
                System.getProperty("os.arch") +
                System.getenv("COMPUTERNAME");

        return hashString(hwid);
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUsername() {
        return System.getProperty("user.name");
    }

    public String pasteString() {
        return GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
    }

    public boolean ctrlIsDown() {
        return GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public static String getGreetingMessage() {
        LocalTime currentTime = LocalTime.now();

        String greeting;
        if (currentTime.isBefore(LocalTime.NOON)) {
            greeting = "Доброе утро";
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            greeting = "Добрый день";
        } else if (currentTime.isBefore(LocalTime.of(22, 0))) {
            greeting = "Добрый вечер";
        } else {
            greeting = "Доброй ночи";
        }
        return greeting;
    }

    private static boolean pvpMode;
    private static UUID uuid;
    public static String state = "";
    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;

    public static void startRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize("1248974948282929152", eventHandlers, true, null);
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageText = "Билд: " + Evaware.build + " | PC: " + ClientUtility.getUsername();
        discordRPC.Discord_UpdatePresence(discordRichPresence);


        new Thread(() -> {
            while (true) {
                try {
                    if (mc.currentScreen instanceof MainMenuScreen || mc.currentScreen instanceof MainScreen) {
                        state = "В главном меню";
                    } else if (mc.currentScreen instanceof MultiplayerScreen) {
                        state = "Выбирает сервер";
                    } else if (mc.isSingleplayer()) {
                        state = "В одиночном мире";
                    } else if (mc.getCurrentServerData() != null) {
                        String serverIP = mc.getCurrentServerData().serverIP.replace("mc.", "").replace("play.", "").replace("gg.", "").replace("go.", "").replace("join.", "").replace("creative.", "")
                                .replace(".top", "").replace(".pro", "").replace(".ru", "").replace(".cc", "").replace(".space", "").replace(".eu", "").replace(".com", "").replace(".net", "").replace(".xyz", "").replace(".gg", "").replace(".me", "").replace(".su", "").replace(".fun", "").replace(".org", "").replace(".host", "")
                                .replace("localhost", "LocalServer").replace(":25565", "")
                        ;

                        if (serverIP.toLowerCase().contains("aternos")) {
                            state = "Играет на приватном сервере";
                        } else {
                            state = "Играет на " + serverIP;
                        }
                    } else if (mc.currentScreen instanceof OptionsScreen) {
                        state = "В настройках";
                    } else if (mc.currentScreen instanceof WorldSelectionScreen) {
                        state = "Выбирает мир";
                    } else if (mc.currentScreen instanceof AltScreen) {
                        state = "В меню выбора аккаунтов";
                    } else {
                        state = "Загрузка...";
                    }

                    discordRichPresence.largeImageKey = "eva";
                    discordRichPresence.details = state;
                    discordRichPresence.state = "Моды: " + Evaware.getInst().getModuleManager().countEnabledModules() + "/" + Evaware.getInst().getModuleManager().getModules().size();
                    discordRPC.Discord_UpdatePresence(discordRichPresence);

                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }
            }
        }).start();
    }

    public static void stopRPC() {
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }

    public void updateBossInfo(SUpdateBossInfoPacket packet) {
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
    public boolean isConnectedToServer(String ip) {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && mc.getCurrentServerData().serverIP.contains(ip);
    }
    public boolean isPvP() {
        return pvpMode;
    }

    public int calc(int value) {
        MainWindow rs = mc.getMainWindow();
        return (int) (value * rs.getGuiScaleFactor() / 2);
    }

    public Vector2i getMouse(int mouseX, int mouseY) {
        return new Vector2i((int) (mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2), (int) (mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2));
    }
}
