package eva.ware.modules.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleRegister {
    String name();
    int key() default 0;
    Category category();
}
