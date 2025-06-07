package newcode.fun.control.cmd;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface CmdInfo {
    String name();
    String description();
    String descriptionen();
}
