package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import eva.ware.events.EventPacket;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeListSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SExplosionPacket;

@Getter
@ModuleRegister(name = "AntiPush", category = Category.Misc)
public class AntiPush extends Module {

    public static ModeListSetting modes = new ModeListSetting("Тип",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Вода", false),
            new CheckBoxSetting("Взрывы", false),
            new CheckBoxSetting("Блоки", true));

    public AntiPush() {
        addSettings(modes);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (modes.is("Взрывы").getValue()) {
                if (e.getPacket() instanceof SExplosionPacket) {
                    e.cancel();
                }
            }
        }
    }
}
