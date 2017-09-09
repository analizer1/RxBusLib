package net.analizer.rxbuslib.annotations;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

import rx.Observer;
import rx.Subscription;
import rx.subjects.Subject;

@SuppressWarnings("WeakerAccess")
public class SourceMethod {
    /**
     * The listener object
     */
    public Object listener;

    /**
     * The subscription method
     */
    public Method method;

    /**
     * Class type of the method's parameter
     */
    public Class<?> parameterClass;

    /**
     * Method observer
     */
    public Observer<Object> observer;

    /**
     * The subscription of the observer
     */
    Subscription subscription;

    /**
     * This is the instant id of the parent class or object
     */
    private int instanceId;

    public SourceMethod(@NonNull Method method,
                        @NonNull Class<?> parameterClass,
                        @NonNull Object listener) {

        this.method = method;
        this.parameterClass = parameterClass;
        this.listener = listener;
        this.instanceId = System.identityHashCode(listener);

        this.observer = new Observer<Object>() {
            @Override
            public void onCompleted() {
                // TODO: 9/9/17 handle onComplete SourceMethod
            }

            @Override
            public void onError(Throwable e) {
                // TODO: 9/9/17 handle error SourceMethod
            }

            @Override
            public void onNext(Object event) {
                invoke(event);
            }
        };
    }

    /**
     * Check if the method is a member of that the same class and instance.
     *
     * @param listener an instance of a listener class
     * @return TRUE if this method is a member of the listener class and of the same instance.
     */
    public boolean isMemberOf(Object listener) {
        return this.listener.getClass() == listener.getClass()
                && instanceId == System.identityHashCode(listener);
    }

    /**
     * Subscribe this method observer to a Subject
     *
     * @param subject Subject to be observed
     * @return Subscription
     */
    public Subscription subscribeTo(@NonNull Subject<Object, Object> subject) {
        if (subscription == null) {
            subscription = subject.subscribe(observer);
        }
        return subscription;
    }

    /**
     * Un-subscribe to the previously subscribed Subject
     */
    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    /**
     * Execute the actual method
     *
     * @param events parameters to be passed into invoking method
     */
    private void invoke(Object... events) {
        try {
            method.invoke(listener, events);

        } catch (Exception e) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !SourceMethod.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        // has to be the same method of the same object instance
        SourceMethod sourceMethod = (SourceMethod) obj;
        return instanceId == sourceMethod.instanceId
                && method.equals(sourceMethod.method)
                && parameterClass == sourceMethod.parameterClass
                && listener.getClass() == sourceMethod.listener.getClass();
    }
}
