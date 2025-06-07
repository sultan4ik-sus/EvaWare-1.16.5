package newcode.fun.module.impl.player;

import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "ClientSounds", type = TypeList.Player)
public class ClientSounds extends Module {

    public final SliderSetting voulme = new SliderSetting("Громкость", 60f, 5f, 100f, 5f);

    public ClientSounds() {
        super();
        addSettings(voulme);
    }

    @Override
    public boolean onEvent(Event event) {

        return false;
    }
}
