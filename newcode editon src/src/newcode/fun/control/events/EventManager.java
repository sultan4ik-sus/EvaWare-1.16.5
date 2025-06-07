package newcode.fun.control.events;

import net.minecraft.client.Minecraft;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;

public class EventManager {

    /**
     * Вызывает событие и передает его всем активным модулям для обработки.
     *
     * @param event событие для вызова.
     */
    public static void call(final Event event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().world == null) {
            return;
        }

        if (event.isCancel()) {
            return;
        }

        callEvent(event);
    }

    /**
     * Вызывает указанное событие и передает его всем активным модулям для обработки.
     *
     * @param event событие для вызова.
     */
    private static void callEvent(Event event) {
        for (final Module module : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (!module.isState())
                continue;

            module.onEvent(event);
        }
    }
}