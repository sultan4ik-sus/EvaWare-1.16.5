package newcode.fun.module.impl.player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.packet.EventPacket;
import newcode.fun.control.events.impl.player.EventUpdate;
import newcode.fun.control.events.impl.player.Listener;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.api.Module;
import newcode.fun.module.settings.Setting;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.utils.Counter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.HashMap;

@Annotation(
        name = "AutoDuel",
        type = TypeList.Player, desc = "Автоматически вызывает игрока на дуэль")

public class AutoDuel extends Module {
    private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
    private final MultiBoxSetting mode = new MultiBoxSetting("Кит", new BooleanOption[]{new BooleanOption("Щит", false), new BooleanOption("Шипы 3", true), new BooleanOption("Лук", false), new BooleanOption("Тотемы", false), new BooleanOption("Исцеление", false), new BooleanOption("Шары", false), new BooleanOption("Классик", false), new BooleanOption("Читерский рай", false), new BooleanOption("Незерка", false)});
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private final List<String> sent = Lists.newArrayList();
    private final Counter counter = Counter.create();
    private final Counter counter2 = Counter.create();
    private final Counter counterChoice = Counter.create();
    private final Counter counterTo = Counter.create();

    // Словарь для хранения времени последней отправки дуэли
    private final HashMap<String, Long> duelTimestamps = new HashMap<>();

    private final Listener<EventUpdate> onUpdate = (event) -> {
        List<String> players = this.getOnlinePlayers();
        Minecraft var10001 = mc;
        double var10000 = Math.pow(this.lastPosX - Minecraft.player.getPosX(), 2.0);
        Minecraft var10002 = mc;
        var10000 += Math.pow(this.lastPosY - Minecraft.player.getPosY(), 2.0);
        var10002 = mc;
        double distance = Math.sqrt(var10000 + Math.pow(this.lastPosZ - Minecraft.player.getPosZ(), 2.0));
        if (distance > 500.0) {
            this.toggle();
        }

        var10001 = mc;
        this.lastPosX = Minecraft.player.getPosX();
        var10001 = mc;
        this.lastPosY = Minecraft.player.getPosY();
        var10001 = mc;
        this.lastPosZ = Minecraft.player.getPosZ();
        if (this.counter2.hasReached(80L * (long)players.size())) {
            this.sent.clear();
            this.counter2.reset();
        }

        Iterator var5 = players.iterator();

        Minecraft var15;
        while(var5.hasNext()) {
            String player = (String)var5.next();
            if (!this.sent.contains(player) && !player.equals(mc.session.getProfile().getName()) && this.counter.hasReached(600L)) {

                if (duelTimestamps.containsKey(player)) {
                    long lastDuelTime = duelTimestamps.get(player);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastDuelTime < 31000) {
                        continue;
                    }
                }

                var15 = mc;
                Minecraft.player.sendChatMessage("/duel " + player);
                this.sent.add(player);
                duelTimestamps.put(player, System.currentTimeMillis());
                this.counter.reset();
            }
        }

        var15 = mc;
        Container patt3185$temp = Minecraft.player.openContainer;
        if (patt3185$temp instanceof ChestContainer chest) {
            Minecraft var10005;
            if (mc.currentScreen.getTitle().getString().contains("Выбор набора (1/1)")) {
                for(int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
                    List<Integer> slotsID = new ArrayList();
                    int index = 0;
                    Iterator var9 = this.mode.getValues().iterator();

                    while(var9.hasNext()) {
                        BooleanOption value = (BooleanOption)var9.next();
                        if (!value.getValue()) {
                            ++index;
                        } else {
                            slotsID.add(index);
                            ++index;
                        }
                    }

                    Collections.shuffle(slotsID);
                    int slotID = (Integer)slotsID.get(0);
                    if (this.counterChoice.hasReached(80L)) {
                        var10005 = mc;
                        mc.playerController.windowClick(chest.windowId, slotID, 0, ClickType.QUICK_MOVE, Minecraft.player);
                        this.counterChoice.reset();
                    }
                }
            } else if (mc.currentScreen.getTitle().getString().contains("Настройка поединка") && this.counterTo.hasReached(80L)) {
                var10005 = mc;
                mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, Minecraft.player);
                this.counterTo.reset();
            }
        }

    };

    private final Listener<EventPacket> onPacket = (event) -> {
        if (event.isReceivePacket()) {
            IPacket<?> packet = event.getPacket();
            if (packet instanceof SChatPacket) {
                SChatPacket chat = (SChatPacket)packet;
                String text = chat.getChatComponent().getString().toLowerCase();
                if (text.contains("начало") && text.contains("через") && text.contains("секунд!") || text.equals("дуэли » во время поединка запрещено использовать команды")) {
                    this.toggle();
                }
            }
        }

    };

    public AutoDuel() {
        this.addSettings(new Setting[]{this.mode});
    }

    private List<String> getOnlinePlayers() {
        Minecraft var10000 = mc;
        return (List)Minecraft.player.connection.getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).filter((profileName) -> {
            return pattern.matcher(profileName).matches();
        }).collect(Collectors.toList());
    }

    public boolean onEvent(Event event) {
        if (event instanceof EventUpdate) {
            this.onUpdate.onEvent((EventUpdate)event);
        } else if (event instanceof EventPacket) {
            this.onPacket.onEvent((EventPacket)event);
        }

        return false;
    }
}
