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

@Annotation(name = "Assistant", type = TypeList.Player, desc = "Ваш Ассистент")
public class Assistent extends Module {
    public ModeSetting mode = new ModeSetting("Выбор режимов", "FunTime", "FunTime", "HolyWorld", "ReallyWorld");

    public BooleanOption dist = new BooleanOption("В радиусе исп трап", true).setVisible(() -> mode.is("FunTime"));
    private BindSetting trap = new BindSetting("Трапа", 0).setVisible(() -> mode.is("FunTime"));

    private BindSetting diz = new BindSetting("Дезорент", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting plast = new BindSetting("Пласт", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting yaw = new BindSetting("Явная пыль", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting aura = new BindSetting("Божья аура", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting death = new BindSetting("Огнен смерч", 0).setVisible(() -> mode.is("FunTime"));
    private BindSetting stick = new BindSetting("Взрыв штучка", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting grav = new BindSetting("Гравитация", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting oglysh = new BindSetting("Оглушение", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting trapka = new BindSetting("Взрыв трап", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting trapk = new BindSetting("Трaпкa", 0).setVisible(() -> mode.is("HolyWorld"));
    private BindSetting antipolet = new BindSetting("Анти Полет", 0).setVisible(() -> mode.is("ReallyWorld"));
    private final TimerUtil timerUtil = new TimerUtil();
    private BindSetting dropKey = new BindSetting("Выброс корд", 0);
    public BooleanOption blockmsg = new BooleanOption("Блок запрет слова", true).setVisible(() -> mode.is("ReallyWorld"));
    public BooleanOption coloserp = new BooleanOption("Не скачивать рп", true).setVisible(() -> mode.is("ReallyWorld"));
    public BooleanOption sell = new BooleanOption("Выделять цены", true).setVisible(() -> mode.is("FunTime"));
    public BooleanOption chatconverto = new BooleanOption("Конвертить время", true).setVisible(() -> mode.is("FunTime"));
    private UUID uuid;
    private final String[] forbiddenWords = {
            "экспа", "экспенсив",
            "экспой", "нуриком",
            "целкой", "нурлан",
            "newcode", "ньюкод",
            "нурсултан", "целестиал",
            "целка", "нурик",
            "атернос", "aternos", "expa",
            "celka", "nurik",
            "expensive", "celestial",
            "nursultan", "фанпей",
            "funpay", "fluger",
            "акриен", "akrien",
            "фантайм", "funtime",
            "безмамный", "rich", "рич",
            "без мамный", "wild",
            "вилд", "excellent",
            "экселлент", "matix",
            "impact", "матикс",
            "импакт", "wurst"};
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
                mc.player.sendChatMessage("!корды " + (int) mc.player.getPosX() + " " + (int) mc.player.getPosZ());
                Manager.NOTIFICATION_MANAGER.add("Ваши координаты были отправлены в чат!", "", 2);
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
                            ClientUtils.sendMessage("" + TextFormatting.GRAY + "Ваше сообщение содержит " + TextFormatting.RED + "запрещенное" + TextFormatting.GRAY + " слово, сообщение не было отправлено");
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
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует анти полет!");
        } else {
            InventoryUtils.antipolet(Items.FIREWORK_STAR, false);
        }
    }

    private void trapka() {
        if (InventoryUtils.getItemSlot(Items.NETHERITE_SCRAP) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует трапка!");
        } else {
            InventoryUtils.inventorySwapClick(Items.NETHERITE_SCRAP, false);
        }
    }

    private void aura() {
        if (InventoryUtils.getItemSlot(Items.PHANTOM_MEMBRANE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует божья аура!");
        } else {
            InventoryUtils.inventorySwapClick(Items.PHANTOM_MEMBRANE, false);
        }
    }

    private void plast() {
        if (InventoryUtils.getItemSlot(Items.DRIED_KELP) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует пласт!");
        } else {
            InventoryUtils.inventorySwapClick(Items.DRIED_KELP, false);
        }
    }

    private void yaw() {
        if (InventoryUtils.getItemSlot(Items.SUGAR) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует явная пыль!");
        } else {
            InventoryUtils.inventorySwapClick(Items.SUGAR, false);
        }
    }

    private void death() {
        if (InventoryUtils.getItemSlot(Items.FIRE_CHARGE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует огненный смерч!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIRE_CHARGE, false);
        }
    }

    private void diz() {
        if (InventoryUtils.getItemSlot(Items.ENDER_EYE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует дезориентация!");
        } else {
            InventoryUtils.inventorySwapClick(Items.ENDER_EYE, false);
        }
    }

    private void stick() {
        if (InventoryUtils.getItemSlot(Items.FIRE_CHARGE) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует взрывная штучка!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FIRE_CHARGE, false);
        }
    }

    private void grav() {
        if (InventoryUtils.getItemSlot(Items.FEATHER) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует гравитация!");
        } else {
            InventoryUtils.inventorySwapClick(Items.FEATHER, false);
        }
    }

    private void oglysh() {
        if (InventoryUtils.getItemSlot(Items.NETHER_STAR) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует оглушение!");
        } else {
            InventoryUtils.inventorySwapClick(Items.NETHER_STAR, false);
        }
    }

    private void trapkaHolyWorld() {
        if (InventoryUtils.getItemSlot(Items.PRISMARINE_SHARD) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует взрывная трапка!");
        } else {
            InventoryUtils.inventorySwapClick(Items.PRISMARINE_SHARD, false);
        }
    }

    private void trapkHolyWorld() {
        if (InventoryUtils.getItemSlot(Items.POPPED_CHORUS_FRUIT) == -1) {
            ClientUtils.sendMessage(TextFormatting.RED + "У вас отсутствует трапка!");
        } else {
            InventoryUtils.inventorySwapClick(Items.POPPED_CHORUS_FRUIT, false);
        }
    }

    private void onPacket(EventPacket packetEvent) {
        IPacket<?> var3 = packetEvent.getPacket();
        SChatPacket chatPacket;
        String chatMessage;
        if (chatconverto.get()) {
            if (var3 instanceof SChatPacket && (chatMessage = (chatPacket = (SChatPacket) var3).getChatComponent().getString().toLowerCase(Locale.ROOT)).contains("до следующего ивента:")) {
                int startIndex = chatMessage.indexOf(":") + 2;
                int endIndex = chatMessage.indexOf(" сек", startIndex);
                if (endIndex == -1) {
                    return;
                }

                String secondsString = chatMessage.substring(startIndex, endIndex);
                int seconds = Integer.parseInt(secondsString.trim());
                String convertedTime = this.convertTime(seconds);
                Manager.NOTIFICATION_MANAGER.add("До следующего ивента: " + convertedTime, "", 5);
            }
        }

    }

    private String convertTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        int remainingSeconds = seconds % 60;
        Object timeString = "";

        if (hours > 0) {
            timeString = (String) timeString + hours + " часов ";
        }

        if (minutes > 0) {
            timeString = (String) timeString + minutes + " минут ";
        }

        if (remainingSeconds > 0 || ((String) timeString).isEmpty()) {
            timeString = (String) timeString + remainingSeconds + " секунд";
        }

        return ((String) timeString).trim();
    }
}
