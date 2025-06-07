package newcode.fun.module.impl.player;

import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;

@Annotation(name = "AutoEat", type = TypeList.Player)
public class AutoEat extends Module {

    private boolean isEating = false;

    @Override
    public boolean onEvent(final Event event) {
        if (mc.player == null || mc.world == null) return false;

        if (event instanceof EventUpdate e) {
            mc.gameSettings.keyBindUseItem.pressed = isEating;

            if (mc.player.getFoodStats().getFoodLevel() < 15) {
                int slot = findEatSlot();

                if (slot == -1) return false;

                mc.player.inventory.currentItem = slot;

                isEating = true;
            } else {
                isEating = mc.player.getFoodStats().needFood();
            }
        }
        return false;
    }

    public int findEatSlot() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);

            if (stack.getUseAction() == UseAction.EAT) {
                return slot;
            }
        }

        return -1;
    }
}
