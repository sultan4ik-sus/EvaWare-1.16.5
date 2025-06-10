package eva.ware.manager.schedules.impl;

import eva.ware.manager.schedules.Schedule;
import eva.ware.manager.schedules.TimeType;

public class CompetitionSchedule extends Schedule {
    @Override
    public String getName() {
        return "Состязание";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.SEVEN_THIRTY_FIVE, TimeType.FIVE, TimeType.TEN_THIRTY_FIVE, TimeType.THIRTEEN_THIRTY_FIVE, TimeType.SIXTEEN_THIRTY_FIVE, TimeType.NINETEEN_THIRTY_FIVE, TimeType.TWENTY_TWO_THIRTY_FIVE, TimeType.ONE_FORTY_FIVE};
    }
}
