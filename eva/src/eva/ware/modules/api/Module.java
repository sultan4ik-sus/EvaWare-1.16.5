package eva.ware.modules.api;

import eva.ware.modules.impl.misc.ClientTune;
import eva.ware.ui.notify.impl.NoNotify;
import eva.ware.ui.notify.impl.SuccessNotify;
import eva.ware.utils.client.SoundPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

import eva.ware.Evaware;
import eva.ware.modules.settings.api.Setting;
import eva.ware.utils.client.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Module implements IMinecraft {

    final String name;
    final Category category;
    boolean enabled;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Module() {
        this.name = getClass().getAnnotation(ModuleRegister.class).name();
        this.category = getClass().getAnnotation(ModuleRegister.class).category();
        this.bind = getClass().getAnnotation(ModuleRegister.class).key();
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void betterComp(Module module) {
        if (getName().equals(module.getName())) {
            return;
        }

        if (module.isEnabled()) {
            module.setEnabled(false, false);
            module.setEnabled(true, false);
            print(getName() + ": улучшил совместимость с модулем " + module.getName());
        }
    }

    public void onEnable() {
        animation.animate(1, 0.25f, Easings.CIRC_OUT);
        Evaware.getInst().getEventBus().register(this);
    }

    public void onDisable() {
        animation.animate(0, 0.25f, Easings.CIRC_OUT);
        Evaware.getInst().getEventBus().unregister(this);
    }


    public void toggle() {
        setEnabled(!enabled, false);
    }

    public final void setEnabled(boolean newState, boolean config) {
        if (enabled == newState) {
            return;
        }

        enabled = newState;

        try {
            if (enabled) {
                onEnable();
                if (!name.equals("ClickGui")) Evaware.getInst().getNotifyManager().add(0, new SuccessNotify(this.name + " | " + TextFormatting.GREEN + "enabled", 1000));
            } else {
                onDisable();
                if (!name.equals("ClickGui")) Evaware.getInst().getNotifyManager().add(0, new NoNotify(this.name + " | " + TextFormatting.RED + "disabled", 1000));
            }
            if (!config) {
                ModuleManager moduleManager = Evaware.getInst().getModuleManager();
                ClientTune clientTune = moduleManager.getClientTune();

                if (clientTune != null && clientTune.isEnabled() && !name.equals("ClickGui")) {
                    String fileName = clientTune.getFileName(enabled);
                    float volume = clientTune.volume.getValue();
                    SoundPlayer.playSound(fileName, volume, false);
                }
            }
        } catch (Exception e) {
            handleException(enabled ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику (@itzremila / .report <message>): " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
