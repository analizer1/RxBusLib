package net.analizer.rxbuslib.events;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.threads.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Wraps a single-argument 'observer' method on a specific object.
 *
 * <p>This class only verifies the suitability of the method and event type if something fails.  Callers are expected to
 * verify their uses of this class.
 *
 * <p>Two SubscriberEvent are equivalent when they refer to the same method on the same object (not class).   This
 * property is used to ensure that no observer method is registered more than once.
 */
@SuppressWarnings("WeakerAccess")
public class SubscriberEvent {

    /**
     * Subscriber method.
     */
    final List<SourceMethod> methodList;

    /**
     * Thread where the Subscriber will observe on
     */
    final EventThread observeThread;

    /**
     * Thread where the Subscriber will subscribe or do the work on
     */
    final EventThread subscribeThread;

    /**
     * RxJava {@link Subject}
     */
    Subject<Object> subject;

    /**
     * Object hash code.
     */
    private final int hashCode;

    /**
     * Create a new subscription
     *
     * @param methodList      List of methods to be invoked
     * @param observeThread   Thread where the Subscriber will observe on
     * @param subscribeThread Thread where the Subscriber will subscribe or do the work on
     */
    public SubscriberEvent(@NonNull List<SourceMethod> methodList,
                           @NonNull EventThread observeThread,
                           @NonNull EventThread subscribeThread) {

        this.hashCode = System.identityHashCode(this);
        this.methodList = new ArrayList<>();
        this.observeThread = observeThread;
        this.subscribeThread = subscribeThread;

        initObservable();
        addMethodIfNotExist(methodList);
    }

    /**
     * UnSubscribe all observers and remove all method's references
     */
    public void unsubscribe() {
        if (methodList != null) {
            synchronized (methodList) {
                for (SourceMethod sourceMethod : methodList) {
                    sourceMethod.unsubscribe();
                }

                subject = null;
                methodList.clear();
            }
        }
    }

    /**
     * Complete the subject and remove all method's reference
     */
    public void complete() {
        if (methodList != null) {
            synchronized (methodList) {
                subject.onComplete();
                methodList.clear();
            }
        }
    }

    /**
     * Remove all methods of the given listener.
     * onComplete() will be invoked if there are no more
     * methods.
     *
     * @param listener the listener class/object to be removed
     * @return the remaining number of subscribing methods
     */
    public int unRegisterListener(@NonNull Object listener) {
        List<SourceMethod> removeList = new ArrayList<>();
        for (SourceMethod sourceMethod : methodList) {
            if (sourceMethod.isMemberOf(listener)) {
                sourceMethod.unsubscribe();
                removeList.add(sourceMethod);
            }
        }

        methodList.removeAll(removeList);

        int cnt = methodList.size();
        if (cnt == 0) {
            complete();
        }

        return cnt;
    }

    /**
     * @param events
     */
    public void emit(@NonNull Object... events) {
        for (Object event : events) {
            subject.onNext(event);
        }
    }

    public Subject getSubject() {
        return subject;
    }

    public int addMethodIfNotExist(@NonNull List<SourceMethod> methodList) {
        int addCnt = 0;
        synchronized (this.methodList) {
            for (SourceMethod sourceMethod : methodList) {
                if (!this.methodList.contains(sourceMethod)) {
                    sourceMethod.method.setAccessible(true);
                    sourceMethod.subscribeTo(subject);
                    this.methodList.add(sourceMethod);
                    addCnt++;
                }
            }
        }

        return addCnt;
    }

    protected void initObservable() {
        subject = PublishSubject.create();

        subject.observeOn(EventThread.getScheduler(observeThread))
                .subscribeOn(EventThread.getScheduler(subscribeThread));
    }

    public List<SourceMethod> getMethodList() {
        return methodList;
    }

    /**
     * Throw a {@link RuntimeException} with given message and cause lifted from an {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not have a
     * cause, neither will the {@link RuntimeException}.
     */
    void throwRuntimeException(String msg, Exception e) {
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
        return "[SubscriberEvent " + methodList + " (" + String.valueOf(hashCode) + ")]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }
}
