package net.analizer.rxbuslib.annotations;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;

@SuppressWarnings("WeakerAccess")
public class SourceMethod<T> implements Observer<T> {
    /**
     * The listener object
     */
    public T listener;

    /**
     * The subscription method
     */
    public Method method;

    /**
     * Class type of the method's parameter
     */
    public Class<?> parameterClass;

    /**
     * The subscription of the observer
     */
    private Disposable mDisposable;

    /**
     * This is the instant id of the parent class or object
     */
    public final int instanceId;

    public SourceMethod(@NonNull Method method,
                        @NonNull Class<?> parameterClass,
                        @NonNull T listener) {

        this.method = method;
        this.parameterClass = parameterClass;
        this.listener = listener;
        this.instanceId = System.identityHashCode(listener);
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
    }

    @Override
    public void onNext(T event) {
        if (isOfSameType(event)) {
            invoke(event);
        }
    }

    @Override
    public void onError(Throwable e) {
        // TODO: 9/9/17 handle error SourceMethod
    }

    @Override
    public void onComplete() {
        // TODO: 9/9/17 handle onComplete SourceMethod
    }

    private boolean isOfSameType(Object event) {
        if (event == null) {
            return false;
        }

        Class<?> aClass = event.getClass();
        if ((aClass.equals(Integer.class) || aClass.equals(Integer.TYPE))
                && (parameterClass.equals(Integer.class) || parameterClass.equals(Integer.TYPE))) {
            return true;

        } else if ((aClass.equals(Long.class) || aClass.equals(Long.TYPE))
                && (parameterClass.equals(Long.class) || parameterClass.equals(Long.TYPE))) {
            return true;

        } else if ((aClass.equals(Character.class) || aClass.equals(Character.TYPE))
                && (parameterClass.equals(Character.class) || parameterClass.equals(Character.TYPE))) {
            return true;

        } else if ((aClass.equals(Boolean.class) || aClass.equals(Boolean.TYPE))
                && (parameterClass.equals(Boolean.class) || parameterClass.equals(Boolean.TYPE))) {
            return true;

        } else if ((aClass.equals(Double.class) || aClass.equals(Double.TYPE))
                && (parameterClass.equals(Double.class) || parameterClass.equals(Double.TYPE))) {
            return true;

        } else if ((aClass.equals(Float.class) || aClass.equals(Float.TYPE))
                && (parameterClass.equals(Float.class) || parameterClass.equals(Float.TYPE))) {
            return true;

        } else if ((aClass.equals(Byte.class) || aClass.equals(Byte.TYPE))
                && (parameterClass.equals(Byte.class) || parameterClass.equals(Byte.TYPE))) {
            return true;

        } else if ((aClass.equals(Short.class) || aClass.equals(Short.TYPE))
                && (parameterClass.equals(Short.class) || parameterClass.equals(Short.TYPE))) {
            return true;

        }
//        else if (aClass.equals(String.class) && (parameterClass.equals(String.class))) {
//            return true;
//        }

        return aClass.equals(parameterClass);
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
    public Disposable subscribeTo(@NonNull Subject<T> subject) {
        if (mDisposable == null) {
            subject.subscribe(this);
        }

        return mDisposable;
    }

    /**
     * Un-subscribe to the previously subscribed Subject
     */
    public void unsubscribe() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        listener = null;
        method = null;
        parameterClass = null;
        mDisposable = null;
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
            String message = e.getMessage();
            if (TextUtils.isEmpty(message)) {
                message = e.getCause().getMessage();
            }

            if (!TextUtils.isEmpty(message)) {
                Log.e("RxBus", message);
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
