package eva.ware.manager.schedules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eva.ware.manager.schedules.impl.*;
import eva.ware.utils.client.IMinecraft;

public class SchedulesManager
        implements IMinecraft {
    private final List<Schedule> schedules = new ArrayList<>();

    public SchedulesManager() {
        this.schedules.addAll(Arrays.asList(new AirDropSchedule(), new ScroogeSchedule(), new SecretMerchantSchedule(), new MascotSchedule(), new CompetitionSchedule()));
    }

    public List<Schedule> getSchedules() {
        return this.schedules;
    }
}