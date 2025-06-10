package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;

import eva.ware.manager.friend.FriendManager;
import eva.ware.events.EventKey;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.BindSetting;
import eva.ware.utils.player.PlayerUtility;
import net.minecraft.entity.player.PlayerEntity;

@ModuleRegister(name = "ClickFriend", category = Category.Misc)
public class ClickFriend extends Module {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.getValue() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerUtility.isNameValid(playerName)) {
                print("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (FriendManager.isFriend(playerName)) {
                FriendManager.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendManager.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
