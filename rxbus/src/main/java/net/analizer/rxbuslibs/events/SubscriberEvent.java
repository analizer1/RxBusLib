package net.analizer.rxbuslibs.events;

import android.support.annotation.NonNull;

import net.analizer.rxbuslibs.annotations.SourceMethod;
import net.analizer.rxbuslibs.threads.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Wraps a single-argument 'subscriber' method on a specific object.
 * <p/>
 * <p>This class only verifies the suitability of the method and event type if something fails.  Callers are expected to
 * verify their uses of this class.
 * <p/>
 * <p>Two SubscriberEvent are equivalent when they refer to the same method on the same object (not class).   This
 * property is used to ensure that no subscriber method is registered more than once.
 */
public class SubscriberEvent {

    /**
     * Object sporting the method.
     */
    final Object target;
    /**
     * Subscriber method.
     */
    final List<SourceMethod> methodList;
    /**
     * Subscriber thread
     */
    final EventThread observeThread;
    final EventThread subscribeThread;
    /**
     * RxJava {@link Subject}
     */
    Subject<Object, Object> subject;
    /**
     * Object hash code.
     */
    private final int hashCode;
    /**
     * Should this Subscriber receive events?
     */
    boolean valid = true;

    private Subscription subscribe;

    public SubscriberEvent(@NonNull Object target,
                           @NonNull List<SourceMethod> methodList,
                           @NonNull EventThread observeThread,
                           @NonNull EventThread subscribeThread) {

        this.target = target;
        this.methodList = methodList;
        this.observeThread = observeThread;
        this.subscribeThread = subscribeThread;

        for (SourceMethod sourceMethod : methodList) {
            sourceMethod.method.setAccessible(true);
        }
        initObservable();

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        hashCode = (prime + methodList.hashCode()) * prime + target.hashCode();
    }

    protected void initObservable() {
        subject = PublishSubject.create();

        subscribe = subject.onBackpressureBuffer()
                .observeOn(EventThread.getScheduler(observeThread))
                .subscribeOn(EventThread.getScheduler(subscribeThread))
                .subscribe(event -> {
                    try {
                        if (valid) {
                            handleEvent(event);
                        }
                    } catch (InvocationTargetException e) {
                        throwRuntimeException("Could not dispatch event: " + event.getClass() + " to subscriber " + SubscriberEvent.this, e);
                    }
                });
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * If invalidated, will subsequently refuse to handle events.
     * <p/>
     * Should be called when the wrapped object is unregistered from the Bus.
     */
    public boolean invalidate() {
        valid = false;
        if (!subject.hasObservers()) {
            return unSubscribe();
        }

        return false;
    }

    public boolean unSubscribe() {
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
            return true;
        }

        return false;
    }

    public boolean unRegisterListener(@NonNull Object listener) {
        List<SourceMethod> removeList = new ArrayList<>();
        for (SourceMethod sourceMethod : methodList) {
            if (sourceMethod.clazz.equals(listener.getClass())) {
                removeList.add(sourceMethod);
            }
        }

        methodList.removeAll(removeList);
        if (methodList.size() == 0) {
            return invalidate();
        }

        return false;
    }

    public void handle(Object event) {
        subject.onNext(event);
    }

    public Subject getSubject() {
        return subject;
    }

    /**
     * Invokes the wrapped subscriber method to handle {@code event}.
     *
     * @param event event to handle
     * @throws IllegalStateException     if previously invalidated.
     * @throws InvocationTargetException if the wrapped method throws any {@link Throwable} that is not
     *                                   an {@link Error} ({@code Error}s are propagated as-is).
     */
    void handleEvent(Object event) throws InvocationTargetException {
        if (!valid) {
            throw new IllegalStateException(toString() + " has been invalidated and can no longer handle events.");
        }
        try {
            for (SourceMethod sourceMethod : methodList) {
                sourceMethod.method.invoke(target, event);
            }

        } catch (IllegalAccessException e) {
            throw new AssertionError(e);

        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Throw a {@link RuntimeException} with given message and cause lifted from an {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not have a
     * cause, neither will the {@link RuntimeException}.
     */
    void throwRuntimeException(String msg, InvocationTargetException e) {
        throwRuntimeException(msg, e.getCause());
    }

    /**
     * Throw a {@link RuntimeException} with given message and cause lifted from an {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not have a
     * cause, neither will the {@link RuntimeException}.
     */
    private void throwRuntimeException(String msg, Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
        } else {
            throw new RuntimeException(msg + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "[SubscriberEvent " + methodList + "]";
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

        final SubscriberEvent other = (SubscriberEvent) obj;

        return target == other.target && SubscriberEvent.class.isAssignableFrom(obj.getClass());
    }
}
