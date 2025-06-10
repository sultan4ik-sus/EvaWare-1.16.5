package eva.ware.command.interfaces;

import eva.ware.command.api.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
