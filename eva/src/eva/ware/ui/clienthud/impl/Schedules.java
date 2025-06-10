package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventUpdate;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.ui.clienthud.updater.ElementUpdater;
import eva.ware.manager.schedules.Schedule;
import eva.ware.manager.schedules.SchedulesManager;
import eva.ware.manager.schedules.TimeType;
import eva.ware.manager.Theme;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Schedules implements ElementRenderer, ElementUpdater {
    final Dragging dragging;
    float width;
    float height;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    final SchedulesManager schedulesManager = new SchedulesManager();
    final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
    List<Schedule> activeSchedules = new ArrayList<>();
    private static final int MINUTES_IN_DAY = 1440;
    boolean sorted = false;

    @Override
    public void update(EventUpdate e) {
        activeSchedules = schedulesManager.getSchedules();
        if (!sorted) {
            this.activeSchedules.sort(Comparator.comparingInt(schedule -> (int) -Fonts.montserrat.getWidth(schedule.getName(), 6.5f)));
            sorted = true;
        }
    }

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        String name = "Schedules";
        RenderUtility.drawStyledRect(ms, posX, posY, width, height);
        int textColor = Theme.textColor;
        ClientFonts.icons_nur[20].drawString(ms, "G", posX + 5, posY + 6.5f, textColor);
        Fonts.montserrat.drawText(ms, name, posX + 10 + 8, posY + padding + 0.5f, textColor, fontSize, 0.07f);

        float maxWidth = Fonts.montserrat.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        posY += fontSize + padding + 2;
        posY += 5f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        for (Schedule schedule : activeSchedules) {
            String nameText = schedule.getName();
            String timeString = getTimeString(schedule);

            float nameWidth = Fonts.montserrat.getWidth(nameText, fontSize);
            float bindWidth = Fonts.montserrat.getWidth(timeString, fontSize);

            float localWidth = nameWidth + bindWidth + padding * 3;

            Fonts.montserrat.drawText(ms, nameText, posX + padding - 0.5f, posY + 2f, textColor, fontSize, .05f);
            Fonts.montserrat.drawText(ms, timeString, posX + width - padding - bindWidth + 1, posY + 2f, textColor, fontSize, .05f);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (fontSize + padding - 3);
            localHeight += (fontSize + padding - 3);
        }
        Scissor.unset();
        Scissor.pop();
        widthAnimation.run(Math.max(maxWidth, 70));
        heightAnimation.run( (localHeight + 5.5f));
        width = (float) widthAnimation.getValue();
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private String formatTime(Calendar calendar, int minutes) {
        int hours = minutes / 60;
        int secondsLeft = 59 - calendar.get(Calendar.SECOND);

        if ((minutes %= 60) > 0) {
            --minutes;
        }

        return hours + "ч " + minutes + "м " + secondsLeft + "с";
    }

    private int calculateTimeDifference(int[] times, int minutes) {
        int index = Arrays.binarySearch(times, minutes);

        if (index < 0) {
            index = -index - 1;
        }

        if (index >= times.length) {
            return times[0] + MINUTES_IN_DAY - minutes;
        }

        return times[index] - minutes;
    }

    private String getTimeString(Schedule schedule, Calendar calendar) {
        int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int[] timeArray = Arrays.stream(schedule.getTimes()).mapToInt(TimeType::getMinutesSinceMidnight).toArray();
        int timeDifference = calculateTimeDifference(timeArray, minutes);
        return formatTime(calendar, timeDifference);
    }

    public String getTimeString(Schedule schedule) {
        return getTimeString(schedule, Calendar.getInstance(timeZone));
    }

}

