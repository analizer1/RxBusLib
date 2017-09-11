package net.analizer.rxbuslib.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        SubscriptionType.PUBLISH,
        SubscriptionType.REPLAY,
        SubscriptionType.BEHAVIOR,
        SubscriptionType.NONE})
@Retention(RetentionPolicy.SOURCE)
public @interface SubscriptionType {
    int PUBLISH = 0;
    int REPLAY = 1;
    int BEHAVIOR = 2;
    int NONE = 3;
}
