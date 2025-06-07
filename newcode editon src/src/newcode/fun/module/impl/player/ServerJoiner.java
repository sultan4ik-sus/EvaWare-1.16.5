package newcode.fun.module.impl.player;

import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.JoinerUtils;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "ServerJoiner", type = TypeList.Player, desc = "������������� ������� �� ������")
public class ServerJoiner extends Module {

    private final ModeSetting ModeJoin = new ModeSetting("����� �������", "���� (1.16.5-1.20.4)", "���� (1.16.5-1.20.4)", "����-����");
    private final SliderSetting griefSelection = new SliderSetting("����� �����", 1, 1, 50, 1).setVisible(() -> ModeJoin.is("���� (1.16.5-1.20.4)"));
    private final TimerUtil timerUtil = new TimerUtil();

    public ServerJoiner() {
        addSettings(ModeJoin, griefSelection);
    }

    @Override
    protected void onEnable() {
        JoinerUtils.selectCompass();
        IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        super.onEnable();
    }

    @Override
    public boolean onEvent(Event event) {
        if (!hasCompassInHand()) {
            ClientUtils.sendMessage("�� ��� �� �������" + TextFormatting.RED + " :)");
            toggle();
            return false;
        }
        if (event instanceof EventUpdate) {
            handleEventUpdate();
        }
        if (event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof SChatPacket packet) {

                String message = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());

                if (message.contains("� ��������� ������ ����������")
                        || message.contains("��������� 20 ������!")
                        || message.contains("������� ����� �������")) {
                    JoinerUtils.selectCompass();
                    IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

                }
            }
        }

        return false;
    }
    private void handleEventUpdate() {
        if (IMinecraft.mc.currentScreen == null) {
            if (IMinecraft.mc.player.ticksExisted < 3)
                IMinecraft.mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        } else if (IMinecraft.mc.currentScreen instanceof ChestScreen) {
            try {
                int numberGrief = griefSelection.getValue().intValue();

                ContainerScreen container = (ContainerScreen) IMinecraft.mc.currentScreen;
                for (int i = 0; i < container.getContainer().inventorySlots.size(); i++) {
                    String s = container.getContainer().inventorySlots.get(i).getStack().getDisplayName().getString();
                    if (ClientUtils.isConnectedToServer("reallyworld")) {
                        switch (ModeJoin.getIndex()) {
                            case 1 -> {
                                if (s.contains("����")) {
                                    if (timerUtil.hasTimeElapsed(50)) {
                                        IMinecraft.mc.playerController.windowClick(IMinecraft.mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, IMinecraft.mc.player);
                                        timerUtil.reset();
                                    }
                                }
                            }
                            case 0 -> {
                                if (s.contains("���������� ���������")) {
                                    if (timerUtil.hasTimeElapsed(50)) {
                                        IMinecraft.mc.playerController.windowClick(IMinecraft.mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, IMinecraft.mc.player);
                                        timerUtil.reset();
                                    }
                                }
                            }
                        }
                    }

                    if (s.contains("���� #" + numberGrief + " (1.16.5+)")) {
                        if (timerUtil.hasTimeElapsed(50)) {
                            IMinecraft.mc.playerController.windowClick(IMinecraft.mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, IMinecraft.mc.player);
                            timerUtil.reset();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

    }
    private boolean hasCompassInHand() {
        for (int i = 0; i < 9; i++) {
            if (IMinecraft.mc.player.inventory.getStackInSlot(i).getItem() == net.minecraft.item.Items.COMPASS) {
                return true;
            }
        }
        return false;
    }
}