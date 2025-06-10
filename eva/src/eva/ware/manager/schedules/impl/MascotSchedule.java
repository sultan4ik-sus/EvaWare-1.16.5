package eva.ware.manager.schedules.impl;

import eva.ware.manager.schedules.Schedule;
import eva.ware.manager.schedules.TimeType;

public class MascotSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Талисман";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.NINETEEN_HALF};
    }
}
