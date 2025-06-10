package eva.ware.ui.clienthud.updater;

import eva.ware.events.EventRender2D;
import eva.ware.utils.client.IMinecraft;

public interface ElementRenderer extends IMinecraft {
    void render(EventRender2D eventRender2D);
}
