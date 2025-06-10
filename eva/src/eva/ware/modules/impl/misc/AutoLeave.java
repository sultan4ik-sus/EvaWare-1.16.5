package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.ModeSetting;
import eva.ware.modules.settings.impl.SliderSetting;
import eva.ware.utils.player.PlayerUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleRegister(name = "AutoLeave", category = Category.Misc)
public class AutoLeave extends Module {

    final ModeSetting action = new ModeSetting("Действие", "Kick", "Kick", "/hub", "/spawn", "/home");
    final SliderSetting distance = new SliderSetting("Дистанция", 50.0f, 1.0f, 100.0f, 1.0f);

    public AutoLeave() {
        addSettings(action, distance);
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        mc.world.getPlayers().stream()
                .filter(this::isValidPlayer)
                .findFirst()
                .ifPresent(this::performAction);
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player.isAlive()
                && player.getHealth() > 0.0f
                && player.getDistance(mc.player) <= distance.getValue()
                && player != mc.player
                && PlayerUtility.isNameValid(player.getName().getString())
                && !FriendManager.isFriend(player.getName().getString());
    }

    private void performAction(PlayerEntity player) {
        if (!action.getValue().equalsIgnoreCase("Kick")) {
            mc.player.sendChatMessage(action.getValue());
            mc.ingameGUI.func_238452_a_(new StringTextComponent("[AutoLeave] " + player.getGameProfile().getName()),
                    new StringTextComponent("test"), -1,
                    -1, -1);
        } else {
            mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("Вы вышли с сервера! \n" + player.getGameProfile().getName()));
        }
        toggle();
    }
}
