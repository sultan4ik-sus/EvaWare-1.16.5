    package newcode.fun.module.impl.player;

    import net.minecraft.client.gui.screen.ChatScreen;
    import net.minecraft.client.gui.screen.EditSignScreen;
    import net.minecraft.client.gui.screen.inventory.InventoryScreen;
    import net.minecraft.client.settings.KeyBinding;
    import net.minecraft.client.util.InputMappings;
    import net.minecraft.network.IPacket;
    import net.minecraft.network.play.client.CClickWindowPacket;
    import newcode.fun.control.events.Event;
    import newcode.fun.control.events.impl.packet.EventPacket;
    import newcode.fun.control.events.impl.player.EventCloseWindow;
    import newcode.fun.control.events.impl.player.EventUpdate;
    import newcode.fun.module.api.Annotation;
    import newcode.fun.module.api.Module;
    import newcode.fun.module.TypeList;
    import newcode.fun.module.settings.imp.BooleanOption;
    import newcode.fun.utils.misc.TimerUtil;
    import newcode.fun.utils.move.MoveUtil;

    import java.util.ArrayList;
    import java.util.List;

    @Annotation(name = "InventoryMove", type = TypeList.Player)
    public class InventoryMove extends Module {

        private final List<IPacket<?>> packet = new ArrayList<>();
        TimerUtil timerUtility = new TimerUtil();
        private final BooleanOption funtime = (new BooleanOption("Îáõîä FunTime", false));

        public InventoryMove(){
            addSettings(funtime);
        }

        @Override
        public boolean onEvent(final Event event) {
            if(event instanceof EventCloseWindow e){
                if (funtime.get()) {
                    if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtil.isMoving()) {
                        new Thread(() -> {
                            timerUtility.reset();
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            for (IPacket p : packet) {
                                mc.player.connection.sendPacketWithoutEvent(p);
                            }
                            packet.clear();
                        }).start();
                        e.setCancel(true);
                    }
                }
            }
            if(event instanceof EventPacket e){
                if (funtime.get()) {
                    if (e.getPacket() instanceof CClickWindowPacket p && MoveUtil.isMoving()) {
                        if (mc.currentScreen instanceof InventoryScreen) {
                            packet.add(p);
                            e.setCancel(true);
                        }
                    }
                }
            }
            if (event instanceof EventUpdate) {
                if (mc.player != null) {
                    final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                            mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                            mc.gameSettings.keyBindSprint};
                    if (funtime.get()) {
                        if (!timerUtility.hasTimeElapsed(400)) {
                            for (KeyBinding keyBinding : pressedKeys) {
                                keyBinding.setPressed(false);
                            }
                            return false;
                        }
                    }

                    if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                        return false;
                    }

                    for (KeyBinding keyBinding : pressedKeys) {
                        boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
                        keyBinding.setPressed(isKeyPressed);
                    }
                }
            }
            return false;
        }
    }

