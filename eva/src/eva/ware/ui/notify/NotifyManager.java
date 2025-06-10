package eva.ware.ui.notify;

import com.google.common.eventbus.Subscribe;
import eva.ware.Evaware;
import eva.ware.events.EventRender2D;
import eva.ware.modules.impl.misc.SelfDestruct;
import eva.ware.ui.notify.impl.NoNotify;
import eva.ware.ui.notify.impl.SuccessNotify;
import eva.ware.ui.notify.impl.WarningNotify;
import eva.ware.utils.client.IMinecraft;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class NotifyManager extends ArrayList<Notify> implements IMinecraft {

    public void init() {
        Evaware.getInst().getEventBus().register(this);
    }

    public void register(final String content, final NotifyType type, long delay) {
        final Notify notification = switch (type) {
            case YES -> new SuccessNotify(content, delay);
            case NO -> new NoNotify(content, delay);
            case WARN -> new WarningNotify(content, delay);
        };

        this.add(notification);
    }

    @Subscribe
    public void onRender(EventRender2D e) {
        if (SelfDestruct.unhooked) return;
        if (!Evaware.getInst().getModuleManager().getHud().elements.is("Уведомления").getValue() || !Evaware.getInst().getModuleManager().getHud().isEnabled()) return;

        if (this.size() == 0 || mc.player == null || mc.world == null) return;
        int i = 0;
        Iterator<Notify> iterator = this.iterator();
        try {
            while (iterator.hasNext()) {
                Notify notification = iterator.next();
                notification.render(e.getMatrixStack(), i);
                if (notification.hasExpired()) {
                    iterator.remove();
                }
                i++;
            }
        } catch (ConcurrentModificationException ignored) {
        }

        if (this.size() > 16) {
            this.clear();
        }
    }
}
