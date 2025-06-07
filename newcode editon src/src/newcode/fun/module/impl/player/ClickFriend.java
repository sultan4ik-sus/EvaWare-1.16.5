package newcode.fun.module.impl.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.game.EventKey;
import newcode.fun.control.events.impl.game.EventMouseTick;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BindSetting;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.misc.TimerUtil;

@Annotation(name = "ClickFriend", type = TypeList.Player)
public class ClickFriend extends Module {
    private final TimerUtil timerUtil = new TimerUtil();

    private BindSetting clickKey = new BindSetting("Кнопка", -98);

    public ClickFriend() {
        addSettings(clickKey);
    }

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventKey e) {
            if (e.key == clickKey.getKey()) {
                handleKeyPressEvent();
            }
        }
        return false;
    }

    private void handleKeyPressEvent() {
        if (timerUtil.hasTimeElapsed(50L) &&  mc.pointedEntity instanceof LivingEntity) {
            String entityName = mc.pointedEntity.getName().getString();

            if (Manager.FRIEND_MANAGER.isFriend(entityName)) {
                Manager.FRIEND_MANAGER.removeFriend(entityName);
                displayRemoveFriendMessage(entityName);
            } else {
                Manager.FRIEND_MANAGER.addFriend(entityName);
                displayAddFriendMessage(entityName);
            }
            timerUtil.reset();
        }
    }

    private void displayRemoveFriendMessage(String friendName) {
        ClientUtils.sendMessage(TextFormatting.RESET + "Удалил " + TextFormatting.RED + friendName + TextFormatting.RESET + " из друзей");
    }

    private void displayAddFriendMessage(String friendName) {
        ClientUtils.sendMessage(TextFormatting.RESET + "Добавил " + TextFormatting.GREEN + friendName + TextFormatting.RESET + " в друзья");
    }
}
