package net.analizer.rxbuslibs.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.analizer.rxbuslibs.annotations.SubscriptionType;
import net.analizer.rxbuslibs.threads.EventThread;

@SuppressWarnings("WeakerAccess")
public class EventType {

    /**
     * Event Tag
     */
    public final String tag;

    /**
     * Event Clazz
     */
//    public final Class<?> clazz;
    /**
     * Object hash code.
     */
    public final int hashCode;

    /**
     * Subscription type.
     */
    @SubscriptionType
    public final int subscriptionType;

    public EventThread observeOnThread;
    public EventThread subscribeOnThread;

    public EventType(@SubscriptionType int subscriptionType,
                     @NonNull String tag) {
        this(subscriptionType, tag, null, null);
    }

    public EventType(@SubscriptionType int subscriptionType,
//                     @NonNull Class<?> clazz,
                     @NonNull String tag,
                     @Nullable EventThread observeOnThread,
                     @Nullable EventThread subscribeOnThread) {

        this.subscriptionType = subscriptionType;
        this.observeOnThread = observeOnThread;
        this.subscribeOnThread = subscribeOnThread;

        this.tag = tag;
//        this.clazz = clazz;

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        hashCode = (prime + tag.hashCode()) * prime;
    }

    @Override
    public String toString() {
        return "[EventType " + tag + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final EventType other = (EventType) obj;

        return tag.equals(other.tag) && subscriptionType == other.subscriptionType;
    }

}