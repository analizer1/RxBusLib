package net.analizer.rxbuslib.interfaces;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.annotations.SubscriptionType;
import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.threads.EventThread;

import java.util.List;

public interface Bus {
    String DEFAULT_IDENTIFIER = "default";

    void register(@NonNull Object listener);

    void unRegister(@NonNull Object listener);

    void post(@SubscriptionType int subscriptionType, Object event, @NonNull String... tags);

    void postPublish(@NonNull Object event, @NonNull String... tags);

    void postReplay(@NonNull Object event, @NonNull String... tags);

    void postBehavior(@NonNull Object event, @NonNull String... tags);

    void createSubscription(@NonNull EventType eventType,
                     @NonNull List<SourceMethod> methodList,
                     @NonNull EventThread observeThread,
                     @NonNull EventThread subscribeThread);

    void removeSubscription(@NonNull EventType eventType);
}
