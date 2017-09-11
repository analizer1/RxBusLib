package net.analizer.rxbuslibs.events;

import android.support.annotation.NonNull;

import net.analizer.rxbuslibs.annotations.SourceMethod;
import net.analizer.rxbuslibs.threads.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import rx.subjects.ReplaySubject;

/**
 * Wraps a single-argument 'subscriber' method on a specific object.
 * <p/>
 * <p>This class only verifies the suitability of the method and event type if something fails.  Callers are expected to
 * verify their uses of this class.
 * <p/>
 * <p>Two SubscriberEvent are equivalent when they refer to the same method on the same object (not class).   This
 * property is used to ensure that no subscriber method is registered more than once.
 */
public class SubscriberReplayEvent extends SubscriberEvent {

    public SubscriberReplayEvent(@NonNull Object target,
                                 @NonNull List<SourceMethod> methodList,
                                 @NonNull EventThread observeThread,
                                 @NonNull EventThread subscribeThread) {
        super(target, methodList, observeThread, subscribeThread);
    }

    @Override
    protected final void initObservable() {
        subject = ReplaySubject.create();
        subject.onBackpressureBuffer()
                .observeOn(EventThread.getScheduler(observeThread))
                .subscribeOn(EventThread.getScheduler(subscribeThread))
//                .subscribe(new Observer<Object>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(Object event) {
//                        try {
//                            if (valid) {
//                                handleEvent(event);
//                            }
//                        } catch (InvocationTargetException e) {
//                            throwRuntimeException("Could not dispatch event: " + event.getClass() + " to subscriber " + SubscriberReplayEvent.this, e);
//                        }
//                    }
//                });
                .subscribe(event -> {
                    try {
                        if (valid) {
                            handleEvent(event);
                        }
                    } catch (InvocationTargetException e) {
                        throwRuntimeException("Could not dispatch event: " + event.getClass() + " to subscriber " + SubscriberReplayEvent.this, e);
                    }
                });
    }

    @Override
    public String toString() {
        return "[SubscriberReplayEvent " + methodList + "]";
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

        final SubscriberReplayEvent other = (SubscriberReplayEvent) obj;

        return target == other.target && SubscriberEvent.class.isAssignableFrom(obj.getClass());
    }

}
