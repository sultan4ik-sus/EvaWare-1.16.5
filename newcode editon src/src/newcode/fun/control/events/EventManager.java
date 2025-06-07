package newcode.fun.control.events;

import net.minecraft.client.Minecraft;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;

public class EventManager {

    /**
     * �������� ������� � �������� ��� ���� �������� ������� ��� ���������.
     *
     * @param event ������� ��� ������.
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
     * �������� ��������� ������� � �������� ��� ���� �������� ������� ��� ���������.
     *
     * @param event ������� ��� ������.
     */
    private static void callEvent(Event event) {
        for (final Module module : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (!module.isState())
                continue;

            module.onEvent(event);
        }
    }
}