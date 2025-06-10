package eva.ware.ui.clienthud.updater;

import eva.ware.events.EventUpdate;
import eva.ware.utils.client.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}
