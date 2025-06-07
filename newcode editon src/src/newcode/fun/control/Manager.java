package newcode.fun.control;

import dev.waveycapes.WaveyCapesBase;
import newcode.fun.control.cmd.CmdManager;
import newcode.fun.control.cmd.macro.MacroManager;
import newcode.fun.control.config.ConfigManager;
import newcode.fun.control.config.LastAccountConfig;
import newcode.fun.control.friend.FriendManager;
import newcode.fun.control.staff.StaffManager;
import newcode.fun.control.notif.NotifManager;
import newcode.fun.ui.alt.AltConfig;
import newcode.fun.ui.alt.AltManager;
import newcode.fun.ui.clickgui.Window;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.control.user.UserManager;

public class Manager {

    public static newcode.fun.module.api.Manager FUNCTION_MANAGER;
    public static CmdManager COMMAND_MANAGER;
    public static FriendManager FRIEND_MANAGER;
    public static MacroManager MACRO_MANAGER;
    public static LastAccountConfig LAST_ACCOUNT_CONFIG;
    public static WaveyCapesBase WAVYCAPES_BASE;
    public static StaffManager STAFF_MANAGER;
    public static Window CLICK_GUI;
    public static ConfigManager CONFIG_MANAGER;
    public static StyleManager STYLE_MANAGER;
    public static UserManager USER_PROFILE;
    public static NotifManager NOTIFICATION_MANAGER;
    public static AltManager ALT;
    public static AltConfig ALT_CONFIG;
}
