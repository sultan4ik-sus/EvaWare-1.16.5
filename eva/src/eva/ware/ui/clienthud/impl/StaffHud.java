package eva.ware.ui.clienthud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import eva.ware.manager.StaffManager;
import eva.ware.events.EventRender2D;
import eva.ware.events.EventUpdate;
import eva.ware.ui.clienthud.updater.ElementRenderer;
import eva.ware.ui.clienthud.updater.ElementUpdater;
import eva.ware.manager.Theme;
import eva.ware.utils.animations.AnimationUtility;
import eva.ware.utils.animations.Direction;
import eva.ware.utils.animations.easing.CompactAnimation;
import eva.ware.utils.animations.easing.Easing;
import eva.ware.manager.drag.Dragging;
import eva.ware.utils.animations.impl.EaseBackIn;
import eva.ware.utils.render.color.ColorUtility;
import eva.ware.utils.render.engine2d.RenderUtility;
import eva.ware.utils.render.other.Scissor;
import eva.ware.utils.render.font.Fonts;
import eva.ware.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffHud implements ElementRenderer, ElementUpdater {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");
    final AnimationUtility animation = new EaseBackIn(300, 1, 1);

    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffManager.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    float width;
    float height;

    @Override
    public void render(EventRender2D eventRender2D) {

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6.5f;
        MatrixStack ms = eventRender2D.getMatrixStack();
        String name = "Online staff";
        float iconSize = 10;
        float nameWidth = Fonts.montserrat.getWidth(name, fontSize, 0.07f);
        int textColor = Theme.textColor;

        boolean isAnyStaffActive = false;

        if (!staffPlayers.isEmpty() || mc.currentScreen instanceof ChatScreen) {
            isAnyStaffActive = true;
        }

        animation.setDirection(isAnyStaffActive ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyStaffActive ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtility.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtility.drawStyledRect(ms, posX, posY, width, height);
        Fonts.montserrat.drawText(ms, name, posX + iconSize + 8, posY + padding + 0.5f, textColor, fontSize, 0.07f);
        ClientFonts.icons_nur[20].drawString(ms, "E", posX + 5, posY + 6f, textColor);
        posY += fontSize + padding + 2;

        float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        posY += 5f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        for (StaffHud.Staff f : staffPlayers) {

            ITextComponent prefix = f.getPrefix();
            float prefixWidth = Fonts.montserrat.getWidth(prefix, fontSize);
            String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
            float staffWdth = Fonts.montserrat.getWidth(staff, fontSize);

            float localWidth = prefixWidth + staffWdth + 3 + padding * 3;
            Fonts.montserrat.drawText(ms, prefix, posX + padding - 0.5f, posY + 2f, fontSize, 255);
            Fonts.montserrat.drawText(ms, staff, posX + padding + prefixWidth - 0.5f, posY + 2f, textColor, fontSize, 0.05f);
            RenderUtility.drawCircle(posX + width - padding - 1, posY + padding + 0.5f, 4, f.getStatus().color);
            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding - 3;
            localHeight += fontSize + padding - 3;
        }
        Scissor.unset();
        Scissor.pop();

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = (float) widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5f));
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE(ColorUtility.rgb(111, 254, 68)),
        VANISHED(ColorUtility.rgb(254, 68, 68)),
        NEAR(ColorUtility.rgb(244, 221, 59));
        public final int color;

        Status(int color) {
            this.color = color;
        }
    }
}
