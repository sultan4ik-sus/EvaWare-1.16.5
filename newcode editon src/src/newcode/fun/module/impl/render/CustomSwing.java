package newcode.fun.module.impl.render;

import newcode.fun.control.events.Event;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;

@Annotation(name = "CustomSwing", type = TypeList.Render, desc = "Меняет анимацию атаки")
public class CustomSwing extends Module {
    public final ModeSetting swordAnim = new ModeSetting("Мод", "Вертикальный", "Вертикальный", "Горизонтальный", "Диагональный");
    public final SliderSetting angle = new SliderSetting("Угол", 110, 0, 360, 5).setVisible(() -> swordAnim.is("Горизонтальный"));
    public final SliderSetting swipePower = new SliderSetting("Сила взмаха", 35, 10, 100, 1);
    public final SliderSetting swipeSpeed = new SliderSetting("Плавность взмаха", 6, 1, 10, 1);
    public static BooleanOption item360 = new BooleanOption("Вращать на 360", false);

    public CustomSwing() {
        addSettings(swordAnim, angle, swipePower, swipeSpeed, item360);
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
