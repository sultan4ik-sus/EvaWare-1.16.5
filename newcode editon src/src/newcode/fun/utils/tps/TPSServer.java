package newcode.fun.utils.tps;

import lombok.Getter;

import java.util.Locale;

@Getter
public class TPSServer {
    private final TPSCalc tpsCalc;

    public TPSServer() {
        this.tpsCalc = new TPSCalc();
    }

    public String getFormattedTPS() {
        float tps = tpsCalc.getTPS();
        return String.format(Locale.US, "%.2f", tps);
    }
}

