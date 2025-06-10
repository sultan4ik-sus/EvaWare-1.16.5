package eva.ware.modules.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomStringUtils;

import eva.ware.Evaware;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.client.ClientUtility;
import eva.ware.utils.math.TimerUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;

@ModuleRegister(name = "SelfDestruct", category = Category.Misc)
public class SelfDestruct extends Module {

    public static boolean unhooked = false;
    public String secret = RandomStringUtils.randomAlphabetic(2);
    public TimerUtility timerUtility = new TimerUtility();

    @Override
    public void onEnable() {
        super.onEnable();
        process();
        print("Что бы вернуть чит напишите в чат " + TextFormatting.RED + secret);
        print("Все сообщения удалятся через 5 секунд");
        timerUtility.reset();

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mc.ingameGUI.getChatGUI().clearChatMessages(false);
            toggle();
        }).start();


        unhooked = true;

    }

    public List<Module> saved = new ArrayList<>();

    public void process() {
        for (Module module : Evaware.getInst().getModuleManager().getModules()) {
            if (module == this) continue;
            if (module.isEnabled()) {
                saved.add(module);
                module.setEnabled(false, false);
            }
        }
        ClientUtility.stopRPC();
        File folder = new File(Minecraft.getInstance().gameDir, "\\saves\\files");
        hiddenFolder(folder, true);
    }

    public void hook() {
        for (Module module : saved) {
            if (module == this) continue;
            if (!module.isEnabled()) {
                module.setEnabled(true, false);
            }
        }
        File folder = new File(Minecraft.getInstance().gameDir, "\\saves\\files");
        hiddenFolder(folder, false);
        ClientUtility.startRPC();
        unhooked = false;
    }

    private void hiddenFolder(File folder, boolean hide) {
        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(true);
            } catch (IOException e) {
                System.out.println("Не удалось скрыть папку: " + e.getMessage());
            }
        }
    }
}
