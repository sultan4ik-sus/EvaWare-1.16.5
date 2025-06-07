package newcode.fun.module.api;

import newcode.fun.module.TypeList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Annotation {

    String name();

    String desc() default "";

    int key() default 0;

    TypeList type();
}
