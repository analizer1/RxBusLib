package net.analizer.rxbuslib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeTag {
    String DEFAULT = "default_RxBus_SubscribeTag";

    String value() default DEFAULT;
}
