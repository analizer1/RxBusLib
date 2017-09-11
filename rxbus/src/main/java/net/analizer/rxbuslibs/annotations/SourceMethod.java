package net.analizer.rxbuslibs.annotations;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

@SuppressWarnings("WeakerAccess")
public class SourceMethod {
    public Method method;
    public final Class<?> clazz;

    public SourceMethod(@NonNull Method method,
                        @NonNull Class<?> clazz) {
        this.method = method;
        this.clazz = clazz;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !SourceMethod.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        SourceMethod sourceMethod = (SourceMethod) obj;
        return method.equals(sourceMethod.method) &&
                clazz == sourceMethod.clazz;
    }
}
