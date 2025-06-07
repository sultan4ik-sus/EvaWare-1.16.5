package newcode.fun.utils;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.impl.packet.EventPacket;

public class ServerTPS {
    private static final float MAX_TPS = 20.0F;
    private static final float NANOSECONDS_PER_TICK = 50_000_000F; // 50ms в наносекундах
    private static final ServerTPS INSTANCE = new ServerTPS();

    private float tps = 20.0F;
    private long lastTime = System.nanoTime();
    private long tickCount = 0;

    private ServerTPS() {}

    private void update() {
        long now = System.nanoTime();
        long elapsed = now - lastTime;

        if (elapsed > 0) {
            float rawTPS = Math.min(MAX_TPS, (tickCount * NANOSECONDS_PER_TICK) / (elapsed / 1_000_000_000F));
            this.tps = (float) round(MathHelper.clamp(rawTPS, 0.0F, MAX_TPS));
        }

        tickCount = 0;
        lastTime = now;
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof SUpdateTimePacket) {
            tickCount++;
            if (tickCount >= 20) { // Обновляем TPS каждые 20 пакетов
                update();
            }
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static float getTPS() {
        return INSTANCE.tps;
    }
}
