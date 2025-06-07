package newcode.fun.module.settings;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class Configurable {

    public ArrayList<Setting> settingList = new ArrayList<>();
    public boolean expanded;

    public void addSettings(Setting... options) {
        settingList.addAll(Arrays.asList(options));
    }
}
