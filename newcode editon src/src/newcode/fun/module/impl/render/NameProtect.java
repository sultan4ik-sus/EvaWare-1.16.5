package newcode.fun.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import newcode.fun.control.events.Event;
import newcode.fun.module.TypeList;
import newcode.fun.module.api.Module;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.TextSetting;
import newcode.fun.utils.ClientUtils;

@Annotation(name = "NameProtect", type = TypeList.Player)
public class NameProtect extends Module {

    public TextSetting name = new TextSetting("Ник", "newcode.fun");
    public BooleanOption friends = new BooleanOption("Друзья", false);

    public NameProtect() {
        addSettings(name, friends);
    }

    @Override
    public boolean onEvent(Event event) {

        return false;
    }

    public String patch(String text) {
        String out = text;
        if (this.state) {
            out = text.replaceAll(Minecraft.getInstance().session.getUsername(), name.text);
        }
        return out;
    }

    public ITextComponent patchFriendTextComponent(ITextComponent text, String name) {
        ITextComponent out = text;
        if (this.friends.get() && this.state) {
            out = ClientUtils.replace(text, name, this.name.text);
        }
        return out;
    }
}
