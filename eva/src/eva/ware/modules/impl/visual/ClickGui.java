package eva.ware.modules.impl.visual;

import com.google.common.eventbus.Subscribe;

import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.CheckBoxSetting;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;

@ModuleRegister(name = "ClickGui", category = Category.Visual)
public class ClickGui extends Module {

    public static CheckBoxSetting gradient = new CheckBoxSetting("Градиент", true);
    public static CheckBoxSetting background = new CheckBoxSetting("Фон", true);
    public static CheckBoxSetting blur = new CheckBoxSetting("Размыть", false);
    public static SliderSetting blurPower = new SliderSetting("Сила размытия", 2, 1, 4, 1).visibleIf(() -> blur.getValue());
    public static CheckBoxSetting images = new CheckBoxSetting("Картинки", true);
    public static ModeSetting imageType = new ModeSetting("Текстура", "MyLove", "MyLove", "CatGirl", "CatGirl2", "Emilia", "Pinky", "KisKis", "FurryMaid", "Nyashka", "Nolik", "Miku", "Novoura", "PSChan").visibleIf(() -> images.getValue());
    
    public ClickGui() {
        addSettings(background, gradient, blur, blurPower, images, imageType);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        toggle();
    }
}
