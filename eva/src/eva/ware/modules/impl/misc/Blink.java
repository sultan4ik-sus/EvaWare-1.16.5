package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.events.EventMotion;
import eva.ware.events.EventPacket;
import eva.ware.events.EventRender3D;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.modules.settings.impl.*;
import eva.ware.utils.math.TimerUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleRegister(name = "Blink", category = Category.Misc)
public class Blink extends Module {

    private final CopyOnWriteArrayList<IPacket> packets = new CopyOnWriteArrayList<>();
    private CheckBoxSetting delay = new CheckBoxSetting("Пульс", false);
    private SliderSetting delayS = new SliderSetting("Задержка", 100, 50, 1000, 50).visibleIf(() -> delay.getValue());

    public Blink() {
        addSettings(delay, delayS);
    }

    private long started;

    @Override
    public void onEnable() {
        super.onEnable();
        started = System.currentTimeMillis();
        lastPos = mc.player.getPositionVec();
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            RenderUtility.drawBox(AxisAlignedBB.fromVector(this.lastPos).expand(0.0, 1.0, 0.0).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z).offset(-0.5, 0.0, -0.5).grow(-0.2, 0.0, -0.2), -1, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    float animation;

    public TimerUtility timerUtility = new TimerUtility();

    Vector3d lastPos = new Vector3d(0, 0, 0);


    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player != null && mc.world != null && !mc.isSingleplayer() && !mc.player.getShouldBeDead()) {
            if (e.isSend()) {
                packets.add(e.getPacket());
                e.cancel();
            }
        } else toggle();
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if ((System.currentTimeMillis() - started) >= 29900) {
            toggle();
        }
        if (delay.getValue() && timerUtility.isReached(delayS.getValue().longValue())) {
            for (IPacket packet : packets) {
                mc.player.connection.getNetworkManager().sendPacketWithoutEvent(packet);
            }
            packets.clear();
            started = System.currentTimeMillis();
            timerUtility.reset();
            lastPos = mc.player.getPositionVec();
        }
    }


    @Override
    public void onDisable() {
        super.onDisable();
        for (IPacket packet : packets) {
            mc.player.connection.sendPacket(packet);
        }

        packets.clear();
    }
}
