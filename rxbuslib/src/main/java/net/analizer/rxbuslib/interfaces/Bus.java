package net.analizer.rxbuslib.interfaces;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.SubscriptionType;

public interface Bus {
    String DEFAULT_IDENTIFIER = "default";

    void register(@NonNull Object object);

    void unRegister(@NonNull Object object);

    void post(@SubscriptionType int subscriptionType, Object event, @NonNull String... tags);

    void postPublish(@NonNull Object event, @NonNull String... tags);

    void postReplay(@NonNull Object event, @NonNull String... tags);

    void postBehavior(@NonNull Object event, @NonNull String... tags);
}
