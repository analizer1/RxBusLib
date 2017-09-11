package net.analizer.rxbuslib;

import android.support.annotation.NonNull;
import android.util.Log;

import net.analizer.rxbuslib.annotations.AnnotationProcessor;
import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.annotations.SubscribeTag;
import net.analizer.rxbuslib.annotations.SubscriptionAnnotationProcessor;
import net.analizer.rxbuslib.annotations.SubscriptionType;
import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.events.SubscriberBehaviorEvent;
import net.analizer.rxbuslib.events.SubscriberEvent;
import net.analizer.rxbuslib.events.SubscriberReplayEvent;
import net.analizer.rxbuslib.interfaces.Bus;
import net.analizer.rxbuslib.threads.ThreadEnforcer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("WeakerAccess")
public class RxBus implements Bus {
    private static final String TAG = "RxBus";

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
    public void register(@NonNull Object listener) {
        if (listener == null) {
            return;
        }

        mEnforcer.enforce(this);

        if (BuildConfig.DEBUG) {
            Log.e("RxBus",
                    "registering "
                            + listener.toString()
                            + " (" + String.valueOf(listener.hashCode()) + ")");
        }

        Map<EventType, List<SourceMethod>> foundSubscribersMap =
                mAnnotationProcessor.findAllSubscribers(listener);

        if (!foundSubscribersMap.isEmpty()) {

            for (EventType eventType : foundSubscribersMap.keySet()) {

                List<SourceMethod> methodList = foundSubscribersMap.get(eventType);

                if (mSubscriberMap.containsKey(eventType)) {
                    SubscriberEvent subscriberEvent = mSubscriberMap.get(eventType);
                    subscriberEvent.addMethodIfNotExist(methodList);

                } else {

                    SubscriberEvent subscriberEvent;
                    if (eventType.subscriptionType == SubscriptionType.REPLAY) {
                        subscriberEvent = new SubscriberReplayEvent(
                                methodList, eventType.observeOnThread, eventType.subscribeOnThread
                        );

                    } else if (eventType.subscriptionType == SubscriptionType.BEHAVIOR) {
                        subscriberEvent = new SubscriberBehaviorEvent(
                                methodList, eventType.observeOnThread, eventType.subscribeOnThread
                        );

                    } else {
                        subscriberEvent = new SubscriberEvent(
                                methodList, eventType.observeOnThread, eventType.subscribeOnThread
                        );
                    }

                    mSubscriberMap.put(eventType, subscriberEvent);
                }
            }
        }
    }

    @Override
    public void unRegister(@NonNull Object listener) {

        if (listener == null) {
            return;
        }

        mEnforcer.enforce(this);

        Map<EventType, List<SourceMethod>> foundSubscribersMap =
                mAnnotationProcessor.findAllSubscribers(listener);

        if (!foundSubscribersMap.isEmpty()) {
            for (EventType eventType : foundSubscribersMap.keySet()) {

                if (mSubscriberMap.containsKey(eventType)) {
                    SubscriberEvent subscriberEvent = mSubscriberMap.get(eventType);
                    int methodLeft = subscriberEvent.unRegisterListener(listener);
                    if (methodLeft == 0) {
                        // no subscriber left then call onComplete
                        // and remove this subscriber event
                        mSubscriberMap.remove(eventType);
                    }
                }
            }
        }
    }

    @Override
    public void post(@SubscriptionType int subscriptionType, Object event, @NonNull String... tags) {
        if (tags == null || tags.length == 0) {
            tags = new String[]{SubscribeTag.DEFAULT};
        }

        for (String tag : tags) {
            EventType eventType = new EventType(subscriptionType, tag);
            SubscriberEvent subscriberEvent = mSubscriberMap.get(eventType);
            if (subscriberEvent != null) {

                if (BuildConfig.DEBUG) {
                    String type = "";
                    switch (subscriptionType) {
                        case SubscriptionType.BEHAVIOR:
                            type = "BEHAVIOR";
                            break;

                        case SubscriptionType.NONE:
                            type = "NONE";
                            break;

                        case SubscriptionType.PUBLISH:
                            type = "PUBLISH";
                            break;

                        case SubscriptionType.REPLAY:
                            type = "REPLAY";
                            break;
                    }

                    Log.e(TAG, String.format("posting [%s] %s tags %s", event, type, Arrays.toString(tags)));
                }

                subscriberEvent.emit(event);

            } else if (BuildConfig.DEBUG) {
                Log.e(TAG, "There are no subscribers");
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

    ConcurrentMap<EventType, SubscriberEvent> getSubscriptions() {
        return mSubscriberMap;
    }
}
