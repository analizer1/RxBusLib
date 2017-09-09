package net.analizer.rxbuslib.annotations;

import net.analizer.rxbuslib.threads.EventThread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeReplay {
    SubscribeTag[] tags() default {};

    EventThread thread() default EventThread.MAIN_THREAD;
    EventThread observeOn() default EventThread.MAIN_THREAD;
    EventThread subscribeOn() default EventThread.NEW_THREAD;
}