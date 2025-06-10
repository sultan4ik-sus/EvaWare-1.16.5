package eva.ware.modules.settings.api;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> visibleIf(Supplier<Boolean> bool);
}