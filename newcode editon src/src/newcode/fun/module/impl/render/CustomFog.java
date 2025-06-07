package newcode.fun.module.impl.render;

import net.optifine.shaders.Shaders;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;

@Annotation(name = "CustomFog", type = TypeList.Render)
public class CustomFog extends Module {


    public SliderSetting power = new SliderSetting("Сила", 20, 5,50, 1);
    public BooleanOption confirm = new BooleanOption("я подтверждаю, что у меня сильный ПК", false);

    public CustomFog() {
        addSettings(power,confirm);
    }

    public boolean firstStart;

    @Override
    protected void onEnable() {
        super.onEnable();
        if (!confirm.get()) {
            ClientUtils.sendMessage("Вы не нажали на галочку!");
            setState(false);
        } else {
            Shaders.setShaderPack(Shaders.SHADER_PACK_NAME_DEFAULT);
        }
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender e) {
            if (e.isRender2D()) {

            }
        }
        return false;
    }

    public int getDepth() {
        return 6;
    }

}
