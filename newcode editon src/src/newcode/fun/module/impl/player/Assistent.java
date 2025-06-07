package newcode.fun.module.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.ModeSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.misc.TimerUtil;
import newcode.fun.utils.world.InventoryUtils;

import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Annotation(name = "Assistant", type = TypeList.Player, desc = "��� ���������")
public class Assistent extends Module {
    public ModeSetting mode = new ModeSetting("����� �������", "FunTime", "FunTime", "HolyWorld", "ReallyWorld");

    public BooleanOption dist = new BooleanOption("� ������� ��� ����", true).setVisible(() -> mode.is("FunTime"));
    private BindSetting trap = new BindSetting("�����", 0).setVisible(() -> mode.is("FunTime"));

    private BindSetting diz = new BindSetting("��������", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting plast = new BindSetting("�����", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting yaw = new BindSetting("����� ����", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting aura = new BindSetting("����� ����", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting death = new BindSetting("����� �����", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting stick = new BindSetting("����� ������", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting grav = new BindSetting("����������", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting oglysh = new BindSetting("���������", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting trapka = new BindSetting("����� ����", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting trapk = new BindSetting("��a��a", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting antipolet = new BindSetting("���� �����", 0).setVisible(() -> mode.is("ReallyWorld"));
    private final TimerUtil timerUtil = new TimerUtil();
    private BindSetting dropKey = new BindSetting("������ ����", 0);
    public BooleanOption blockmsg = new BooleanOption("���� ������ �����", true).setVisible(() -> mode.is("ReallyWorld"));
    public BooleanOption coloserp = new BooleanOption("�� ��������� ��", true).setVisible(() -> mode.is("ReallyWorld"));
    public BooleanOption sell = new BooleanOption("�������� ����", true).setVisible(() -> mode.is("FunTime"));
    public BooleanOption chatconverto = new BooleanOption("���������� �����", true).setVisible(() -> mode.is("FunTime"));
    private UUID uuid;
    private final String[] forbiddenWords = {
            "�����", "���������",
            "������", "�������",
            "������", "������",
            "newcode", "������",
            "���������", "���������",
            "�����", "�����",
            "�������", "aternos", "expa",
            "celka", "nurik",
            "expensive", "celestial",
            "nursultan", "������",
            "funpay", "fluger",
            "������", "akrien",
            "�������", "funtime",
            "���������", "rich", "���",
            "��� ������", "wild",
            "����", "excellent",
            "���������", "matix",
            "impact", "������",
            "������", "wurst"};
    int x = -1, z = -1;

    public Assistent() {
        addSettings(mode, dist, trap, diz, plast, yaw, aura, death, stick, grav, oglysh, trapka, trapk, antipolet, dropKey, blockmsg, coloserp, sell, chatconverto);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventKey e) {
            if (mode.is("FunTime")) {
                if (e.key == trap.getKey()) {
                    if (dist.get()) {
                        Iterator var2 = mc.world.getPlayers().iterator();
                        while (var2.hasNext()) {
                            PlayerEntity entity = (PlayerEntity) var2.next();
                            if (mc.player != entity && mc.player.getDistance(entity) <= 4) {
                                if (timerUtil.hasTimeElapsed(150L)) {
                                    trapka();
                                    this.timerUtil.reset();
                                }
                            }
                        }
                    } else {
                        trapka();
                    }
                }
                if (e.key == diz.getKey()) {
                    diz();
                }
                if (e.key == plast.getKey()) {
                    plast();
                }
                if (e.key == yaw.getKey()) {
                    yaw();
                }
                if (e.key == aura.getKey()) {
                    aura();
                }
                if (e.key == death.getKey()) {
                    death();
                }
            }
            if (mode.is("ReallyWorld")) {
                if (e.key == antipolet.getKey()) {
                    antipolet();
                }
            }
            if (mode.is("HolyWorld")) {
                if (e.key == stick.getKey()) {
                    stick();
                }
                if (e.key == grav.getKey()) {
                    grav();
                }
                if (e.key == oglysh.getKey()) {
                    oglysh();
                }
                if (e.key == trapka.getKey()) {
                    trapkaHolyWorld();
                }
                if (e.key == trapk.getKey()) {
                    trapkHolyWorld();
                }
            }

            if (e.key == dropKey.getKey()) {
                mc.player.sendChatMessage("!����� " + (int) mc.player.getPosX() + " " + (int) mc.player.getPosZ());
                Manager.NOTIFICATION_MANAGER.add("���� ���������� ���� ���������� � ���!", "", 2);
            }
        }

        if (event instanceof EventPacket e) {
            onPacket(e);
            if (mode.is("ReallyWorld") && coloserp.get()) {
                if (e.getPacket() instanceof SSendResourcePackPacket) {
                    Minecraft.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
                    Minecraft.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
                    event.setCancel(true);
                }
            }
        }

        if (event instanceof EventPacket) {
            EventPacket packetEvent = (EventPacket) event;
            if (packetEvent.getPacket() instanceof CChatMessagePacket) {
                CChatMessagePacket chatPacket = (CChatMessagePacket) packetEvent.getPacket();
                String message = chatPacket.getMessage().toLowerCase();

                if (mode.is("ReallyWorld") && blockmsg.get()) {
                    for (String word : forbiddenWords) {
                        if (message.contains(word)) {
                            ClientUtils.sendMessage("" + TextFormatting.GRAY + "���� ��������� �������� " + TextFormatting.RED + "�����������" + TextFormatting.GRAY + " �����, ��������� �� ���� ����������");
                            packetEvent.setCancel(true);
                            return false;
                        }
                    }
                }
            }

        }
        return false;
    }

    private void antipolet() {
        if (InventoryUtils.getItemSlot(Items.FIREWORK_STAR) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ���� �����!");
        } else {
            InventoryUtils.antipolet(Items.FIREWORK_STAR, false);
        }
    }

    private void trapka() {
        if (InventoryUtils.getItemSlot(Items.NETHERITE_SCRAP) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.NETHERITE_SCRAP, false);
        }
    }

    private void aura() {
        if (InventoryUtils.getItemSlot(Items.PHANTOM_MEMBRANE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ����� ����!");
        } else {
            InventoryUtils.inventorySwapClick(Items.PHANTOM_MEMBRANE, false);
        }
    }

    private void plast() {
        if (InventoryUtils.getItemSlot(Items.DRIED_KELP) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� �����!");
        } else {
            InventoryUtils.inventorySwapClick(Items.DRIED_KELP, false);
        }
    }

    private void yaw() {
        if (InventoryUtils.getItemSlot(Items.SUGAR) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ����� ����!");
        } else {
            InventoryUtils.inventorySwapClick(Items.SUGAR, false);
        }
    }

    private void death() {
        if (InventoryUtils.getItemSlot(Items.FIRE_CHARGE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� �������� �����!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIRE_CHARGE, false);
        }
    }

    private void diz() {
        if (InventoryUtils.getItemSlot(Items.ENDER_EYE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� �������������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.ENDER_EYE, false);
        }
    }

    private void stick() {
        if (InventoryUtils.getItemSlot(Items.FIRE_CHARGE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� �������� ������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIRE_CHARGE, false);
        }
    }

    private void grav() {
        if (InventoryUtils.getItemSlot(Items.FEATHER) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ����������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FEATHER, false);
        }
    }

    private void oglysh() {
        if (InventoryUtils.getItemSlot(Items.NETHER_STAR) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ���������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.NETHER_STAR, false);
        }
    }

    private void trapkaHolyWorld() {
        if (InventoryUtils.getItemSlot(Items.PRISMARINE_SHARD) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� �������� ������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.PRISMARINE_SHARD, false);
        }
    }

    private void trapkHolyWorld() {
        if (InventoryUtils.getItemSlot(Items.POPPED_CHORUS_FRUIT) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "� ��� ����������� ������!");
        } else {
            InventoryUtils.inventorySwapClick(Items.POPPED_CHORUS_FRUIT, false);
        }
    }

    private void onPacket(EventPacket packetEvent) {
        IPacket<?> var3 = packetEvent.getPacket();
        SChatPacket chatPacket;
        String chatMessage;
        if (chatconverto.get()) {
            if (var3 instanceof SChatPacket && (chatMessage = (chatPacket = (SChatPacket) var3).getChatComponent().getString().toLowerCase(Locale.ROOT)).contains("�� ���������� ������:")) {
                int startIndex = chatMessage.indexOf(":") + 2;
                int endIndex = chatMessage.indexOf(" ���", startIndex);
                if (endIndex == -1) {
                    return;
                }

                String secondsString = chatMessage.substring(startIndex, endIndex);
                int seconds = Integer.parseInt(secondsString.trim());
                String convertedTime = this.convertTime(seconds);
                Manager.NOTIFICATION_MANAGER.add("�� ���������� ������: " + convertedTime, "", 5);
            }
        }

    }

    private String convertTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        int remainingSeconds = seconds % 60;
        Object timeString = "";

        if (hours > 0) {
            timeString = (String) timeString + hours + " ����� ";
        }

        if (minutes > 0) {
            timeString = (String) timeString + minutes + " ����� ";
        }

        if (remainingSeconds > 0 || ((String) timeString).isEmpty()) {
            timeString = (String) timeString + remainingSeconds + " ������";
        }

        return ((String) timeString).trim();
    }
}
