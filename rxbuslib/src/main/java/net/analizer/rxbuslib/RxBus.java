package net.analizer.rxbuslib;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.AnnotationProcessor;
import net.analizer.rxbuslib.annotations.SubscriptionAnnotationProcessor;
import net.analizer.rxbuslib.annotations.SubscriptionType;
import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.events.SubscriberEvent;
import net.analizer.rxbuslib.interfaces.Bus;
import net.analizer.rxbuslib.threads.ThreadEnforcer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("WeakerAccess")
public class RxBus implements Bus {

    private AnnotationProcessor mAnnotationProcessor;
    private ThreadEnforcer mEnforcer;
    private String mIdentifier;

    /**
     * All registered event subscribers, indexed by event type.
     */
    private final ConcurrentMap<EventType, SubscriberEvent> mSubscriberMap;

    /**
     * Creates a new Bus named "default" that enforces actions on the main thread.
     */
    public RxBus() {
        this(DEFAULT_IDENTIFIER);
    }

    /**
     * Creates a new Bus with the given {@code mIdentifier} that enforces actions on the main thread.
     *
     * @param mIdentifier a brief name for this bus, for debugging purposes.  Should be a valid Java mIdentifier.
     */
    public RxBus(String mIdentifier) {
        this(ThreadEnforcer.MAIN, mIdentifier);
    }

    /**
     * Creates a new Bus named "default" with the given {@code mEnforcer} for actions.
     *
     * @param mEnforcer Thread mEnforcer for register, unregister, and post actions.
     */
    public RxBus(ThreadEnforcer mEnforcer) {
        this(mEnforcer, DEFAULT_IDENTIFIER);
    }

    /**
     * Creates a new Bus with the given {@code mEnforcer} for actions and the given {@code mIdentifier}.
     *
     * @param mEnforcer   Thread mEnforcer for register, unregister, and post actions.
     * @param mIdentifier A brief name for this bus, for debugging purposes.  Should be a valid Java mIdentifier.
     */
    public RxBus(ThreadEnforcer mEnforcer, String mIdentifier) {
        this(mEnforcer, mIdentifier, new SubscriptionAnnotationProcessor());
    }

    /**
     * Test constructor which allows replacing the default {@code Finder}.
     *
     * @param mEnforcer           Thread mEnforcer for register, unregister, and post actions.
     * @param mIdentifier         A brief name for this bus, for debugging purposes.  Should be a valid Java mIdentifier.
     * @param annotationProcessor Used to discover event subscribers and producers when registering/unregistering an object.
     */
    RxBus(ThreadEnforcer mEnforcer, String mIdentifier, AnnotationProcessor annotationProcessor) {
        this.mEnforcer = mEnforcer;
        this.mIdentifier = mIdentifier;
        this.mAnnotationProcessor = annotationProcessor;
        this.mSubscriberMap = new ConcurrentHashMap<>();
    }

    @Override
    public void register(@NonNull Object object) {
        mEnforcer.enforce(this);

        Map<EventType, SubscriberEvent> foundSubscribersMap =
                mAnnotationProcessor.findAllSubscribers(object);

        for (EventType eventType : foundSubscribersMap.keySet()) {
            if (!mSubscriberMap.containsKey(eventType)) {
                SubscriberEvent subscriberEvent = foundSubscribersMap.get(eventType);
                mSubscriberMap.put(eventType, subscriberEvent);
            }
        }
    }

    @Override
    public void unRegister(@NonNull Object object) {
        mEnforcer.enforce(this);

        Map<EventType, SubscriberEvent> foundSubscribersMap =
                mAnnotationProcessor.findAllSubscribers(object);

        for (Map.Entry<EventType, SubscriberEvent> entry : foundSubscribersMap.entrySet()) {

            EventType eventType = entry.getKey();
            SubscriberEvent subscriberEvent = entry.getValue();
            if (subscriberEvent.unRegisterListener(object)) {
                mSubscriberMap.remove(eventType);
            }
        }
    }

    @Override
    public void post(@SubscriptionType int subscriptionType, Object event, @NonNull String... tags) {
        for (String tag : tags) {
            EventType eventType = new EventType(subscriptionType, tag);
            SubscriberEvent subscriberEvent = mSubscriberMap.get(eventType);
            if (subscriberEvent != null) {
                subscriberEvent.handle(event);
            }
        }
    }

    @Override
    public void postPublish(@NonNull Object event, @NonNull String... tags) {
        post(SubscriptionType.PUBLISH, event, tags);
    }

    @Override
    public void postReplay(@NonNull Object event, @NonNull String... tags) {
        post(SubscriptionType.REPLAY, event, tags);
    }

    @Override
    public void postBehavior(@NonNull Object event, @NonNull String... tags) {
        post(SubscriptionType.BEHAVIOR, event, tags);
    }
}
