package newcode.fun.utils.tps;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.impl.packet.EventPacket;

@Getter
public class TPSCalc {
    private float tps = 20;  // Изначальное значение TPS

    public float getTPS() {
        return tps;  // Возвращаем актуальное значение TPS
    }

    public void updateTPS() {
        // Логика для вычисления актуального значения TPS
        // Например, рассчитать на основе времени между тиками
        long currentTime = System.currentTimeMillis();
        // Пример вычислений:
        if (currentTime % 2 == 0) {  // Условие, по которому обновляется TPS
            tps = 18.5f;
        } else {
            tps = 19.8f;
        }
    }
}
