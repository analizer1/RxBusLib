package net.analizer.rxbuslib.events;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.threads.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import rx.subjects.BehaviorSubject;

/**
 * Wraps a single-argument 'subscriber' method on a specific object.
 * <p/>
 * <p>This class only verifies the suitability of the method and event type if something fails.  Callers are expected to
 * verify their uses of this class.
 * <p/>
 * <p>Two SubscriberEvent are equivalent when they refer to the same method on the same object (not class).   This
 * property is used to ensure that no subscriber method is registered more than once.
 */
public class SubscriberBehaviorEvent extends SubscriberEvent {

    public SubscriberBehaviorEvent(@NonNull Object target,
                                   @NonNull List<SourceMethod> methodList,
                                   @NonNull EventThread observeThread,
                                   @NonNull EventThread subscribeThread) {
        super(target, methodList, observeThread, subscribeThread);
    }

    @Override
    protected final void initObservable() {
        subject = BehaviorSubject.create();
        subject.onBackpressureBuffer()
                .observeOn(EventThread.getScheduler(observeThread))
                .subscribeOn(EventThread.getScheduler(subscribeThread))
                .subscribe(event -> {
                    try {
                        if (valid) {
                            handleEvent(event);
                        }
                    } catch (InvocationTargetException e) {
                        throwRuntimeException("Could not dispatch event: " + event.getClass() + " to subscriber " + SubscriberBehaviorEvent.this, e);
                    }
                });
    }

    @Override
    public String toString() {
        return "[SubscriberBehaviorEvent " + methodList + "]";
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

        final SubscriberBehaviorEvent other = (SubscriberBehaviorEvent) obj;

        return target == other.target && SubscriberEvent.class.isAssignableFrom(obj.getClass());
    }

}
