package eva.ware.command.api;

import eva.ware.command.interfaces.Parameters;
import eva.ware.command.interfaces.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
