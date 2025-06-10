package eva.ware.manager.schedules.impl;

import eva.ware.manager.schedules.Schedule;
import eva.ware.manager.schedules.TimeType;

public class ScroogeSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Скрудж";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.FIFTEEN_HALF};
    }
}
