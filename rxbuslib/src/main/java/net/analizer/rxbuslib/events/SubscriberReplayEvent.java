package net.analizer.rxbuslib.events;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.threads.EventThread;

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

    public SubscriberReplayEvent(@NonNull List<SourceMethod> methodList,
                                 @NonNull EventThread observeThread,
                                 @NonNull EventThread subscribeThread) {
        super(methodList, observeThread, subscribeThread);
    }

    @Override
    protected final void initObservable() {
        subject = ReplaySubject.create();
        subject.onBackpressureBuffer()
                .observeOn(EventThread.getScheduler(observeThread))
                .subscribeOn(EventThread.getScheduler(subscribeThread));
    }

    @Override
    public String toString() {
        return "[SubscriberReplayEvent " + methodList + " (" + String.valueOf(hashCode()) + ")]";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
