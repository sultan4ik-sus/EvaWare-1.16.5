package newcode.fun.utils.tps;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import newcode.fun.control.events.impl.packet.EventPacket;

@Getter
public class TPSCalc {
    private float tps = 20;  // ����������� �������� TPS

    public float getTPS() {
        return tps;  // ���������� ���������� �������� TPS
    }

    public void updateTPS() {
        // ������ ��� ���������� ����������� �������� TPS
        // ��������, ���������� �� ������ ������� ����� ������
        long currentTime = System.currentTimeMillis();
        // ������ ����������:
        if (currentTime % 2 == 0) {  // �������, �� �������� ����������� TPS
            tps = 18.5f;
        } else {
            tps = 19.8f;
        }
    }
}
