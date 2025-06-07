package newcode.fun;

import dev.waveycapes.WaveyCapesBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import newcode.fun.module.api.Module;
import newcode.fun.utils.language.Translated;
import org.lwjgl.glfw.GLFW;
import newcode.fun.control.cmd.CmdManager;
import newcode.fun.control.cmd.macro.MacroManager;
import newcode.fun.control.config.ConfigManager;
import newcode.fun.control.config.LastAccountConfig;
import newcode.fun.control.events.EventManager;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.control.friend.FriendManager;
import newcode.fun.control.Manager;
import newcode.fun.control.staff.StaffManager;
import newcode.fun.control.notif.NotifManager;
import newcode.fun.ui.alt.AltConfig;
import newcode.fun.ui.alt.AltManager;
import newcode.fun.ui.clickgui.Window;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.control.drag.DragManager;
import newcode.fun.control.drag.Dragging;
import newcode.fun.utils.render.ShaderUtils;

import java.io.File;
import java.io.IOException;

public class NewCode {
    public static boolean isServer;

    private static final File FIRST_RUN_MARKER = new File("C:\\NewCode\\game\\NewCode\\firstrun.json");

    public final File dir = new File("C:\\NewCode\\game\\NewCode");

    public void init() {
        if (!FIRST_RUN_MARKER.exists()) {
            openLinks();
            try {
                FIRST_RUN_MARKER.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ShaderUtils.init();

        Manager.FUNCTION_MANAGER = new newcode.fun.module.api.Manager();
        Manager.NOTIFICATION_MANAGER = new NotifManager();

        try {
            Manager.STYLE_MANAGER = new StyleManager();
            Manager.STYLE_MANAGER.init();

            Manager.ALT = new AltManager();

            ConfigManager configManager = new ConfigManager();
            configManager.init();
            Translated.setRussian(ConfigManager.loadLanguageSetting());

            if (!dir.exists()) {
                dir.mkdirs();
            }
            Manager.ALT_CONFIG = new AltConfig();
            Manager.ALT_CONFIG.init();

            Manager.FRIEND_MANAGER = new FriendManager();
            Manager.FRIEND_MANAGER.init();

            Manager.COMMAND_MANAGER = new CmdManager();
            Manager.COMMAND_MANAGER.init();

            Manager.STAFF_MANAGER = new StaffManager();
            Manager.STAFF_MANAGER.init();

            Manager.MACRO_MANAGER = new MacroManager();
            Manager.MACRO_MANAGER.init();

            Manager.LAST_ACCOUNT_CONFIG = new LastAccountConfig();
            Manager.LAST_ACCOUNT_CONFIG.init();

            Manager.CONFIG_MANAGER = new ConfigManager();
            Manager.CONFIG_MANAGER.init();

            Manager.CLICK_GUI = new Window(new StringTextComponent("A"));
            DragManager.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Manager.WAVYCAPES_BASE = new WaveyCapesBase();
        Manager.WAVYCAPES_BASE.init();
        ClientUtils.startRPC();
    }

    public static void shutDown() {
        Manager.CONFIG_MANAGER.saveConfiguration("default");
        Manager.LAST_ACCOUNT_CONFIG.updateFile();
        Manager.ALT_CONFIG.updateFile();
        DragManager.save();
    }

    private void openLinks() {
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("rundll32 url.dll,FileProtocolHandler https://t.me/newcodecc");
            rt.exec("rundll32 url.dll,FileProtocolHandler https://discord.gg/6rVXTMzFnt");
            rt.exec("rundll32 url.dll,FileProtocolHandler https://vk.com/newcodecc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void keyPress(int key) {
        EventManager.call(new EventKey(key));
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(Manager.CLICK_GUI);
        }
        if (Manager.MACRO_MANAGER != null) {
            Manager.MACRO_MANAGER.onKeyPressed(key);
        }
        for (Module m : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (m.bind == key) {
                m.toggle();
            }
        }
    }

    public static Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }
}
