package eva.ware.modules.impl.misc;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import eva.ware.events.EventUpdate;
import eva.ware.events.EventPacket;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.utils.math.TimerUtility;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ModuleRegister(name = "AutoDuel", category = Category.Misc)
public class AutoDuel extends Module {

    private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
    private final ModeSetting mode = new ModeSetting("Mode", "Шары", "Шары", "Щит", "Шипы 3", "Незеритка", "Читерский рай", "Лук", "Классик", "Тотемы", "Нодебафф");
    private double lastPosX, lastPosY, lastPosZ;

    public AutoDuel() {
        addSettings(mode);
    }


    private final List<String> sent = Lists.newArrayList();

    private final TimerUtility counter = new TimerUtility();
    private final TimerUtility counter2 = new TimerUtility();
    private final TimerUtility counterChoice = new TimerUtility();
    private final TimerUtility counterTo = new TimerUtility();

    @Subscribe
    private void onUpdt(EventUpdate e) {
        final List<String> players = getOnlinePlayers();
        double distance = Math.sqrt(Math.pow(lastPosX - mc.player.getPosX(), 2) + Math.pow(lastPosY - mc.player.getPosY(), 2) + Math.pow(lastPosZ - mc.player.getPosZ(), 2));

        if (distance > 500) {
            toggle();
        }

        lastPosX = mc.player.getPosX();
        lastPosY = mc.player.getPosY();
        lastPosZ = mc.player.getPosZ();

        if (counter2.isReached(800L * players.size())) {
            sent.clear();
            counter2.reset();
        }

        for (final String player : players) {
            if (!sent.contains(player) && !player.equals(mc.session.getProfile().getName())) {
                if (counter.isReached(1000)) {
                    mc.player.sendChatMessage("/duel " + player);
                    sent.add(player);
                    counter.reset();
                }
            }
        }


        if (mc.player.openContainer instanceof ChestContainer chest) {
            if (mc.currentScreen.getTitle().getString().contains("Выбор набора (1/1)")) {
                for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
                    final List<Integer> slotsID = new ArrayList<>();

                    int index = 0;
                    slotsID.add(index);
                    index++;


                    Collections.shuffle(slotsID);

                    if (counterChoice.isReached(150)) {
                        if (mode.is("Щит")) {
                            mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Шипы 3")) {
                            mc.playerController.windowClick(chest.windowId, 1, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Лук")) {
                            mc.playerController.windowClick(chest.windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Тотемы")) {
                            mc.playerController.windowClick(chest.windowId, 3, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Нодебафф")) {
                            mc.playerController.windowClick(chest.windowId, 4, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Шары")) {
                            mc.playerController.windowClick(chest.windowId, 5, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Классик")) {
                            mc.playerController.windowClick(chest.windowId, 6, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Читерский рай")) {
                            mc.playerController.windowClick(chest.windowId, 7, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (mode.is("Незерка")) {
                            mc.playerController.windowClick(chest.windowId, 8, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        counterChoice.reset();
                    }
                }
            } else if (mc.currentScreen.getTitle().getString().contains("Настройка поединка")) {
                if (counterTo.isReached(150)) {
                    mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                    counterTo.reset();
                }
            }
        }
    }

    @Subscribe
    private void onPacket(EventPacket event) {
        if (event.isReceive()) {
            IPacket<?> packet = event.getPacket();

            if (packet instanceof SChatPacket chat) {
                final String text = chat.getChatComponent().getString().toLowerCase();
                if ((text.contains("начало") && text.contains("через") && text.contains("секунд!")) || (text.equals("дуэли » во время поединка запрещено использовать команды"))) {
                    toggle();
                }
            }
        }
    }

    private List<String> getOnlinePlayers() {
        return mc.player.connection.getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).filter(profileName -> pattern.matcher(profileName).matches()).collect(Collectors.toList());
    }

}
