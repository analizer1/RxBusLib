package net.analizer.rxbuslib.annotations;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.threads.EventThread;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionAnnotationProcessor implements AnnotationProcessor {
    @Override
    public Map<EventType, List<SourceMethod>> findAllSubscribers(@NonNull Object listener) {

        Map<EventType, List<SourceMethod>> annotatedMethods = new HashMap<>();
        for (Method method : listener.getClass().getDeclaredMethods()) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored for
            // subscribe/produce.
            if (method.isBridge()) {
                continue;
            }

            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }

                Class<?> parameterClazz = parameterTypes[0];
                if (parameterClazz.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + parameterClazz
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + parameterClazz
                            + " but is not 'public'.");
                }

                Subscribe annotation = method.getAnnotation(Subscribe.class);
                EventThread observeOnThread = annotation.observeOn();
                EventThread subscribeOnThread = annotation.observeOn();
                SubscribeTag[] tags = annotation.tags();
                int tagLength = tags.length;
                do {
                    String tag = SubscribeTag.DEFAULT;
                    if (tagLength > 0) {
                        tag = tags[tagLength - 1].value();
                    }
//                    EventType eventType = new EventType(SubscriptionType.PUBLISH, parameterClazz, tag, observeOnThread, subscribeOnThread);
                    EventType eventType = new EventType(SubscriptionType.PUBLISH, tag, observeOnThread, subscribeOnThread);
                    List<SourceMethod> methodList = annotatedMethods.get(eventType);
                    if (methodList == null) {
                        methodList = new ArrayList<>();
                    }
                    methodList.add(new SourceMethod(method, parameterClazz, listener));
                    annotatedMethods.put(eventType, methodList);
                    tagLength--;
                } while (tagLength > 0);

            } else if (method.isAnnotationPresent(SubscribeReplay.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeReplay annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }

                Class<?> parameterClazz = parameterTypes[0];
                if (parameterClazz.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeReplay annotation on " + parameterClazz
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeReplay annotation on " + parameterClazz
                            + " but is not 'public'.");
                }

                SubscribeReplay annotation = method.getAnnotation(SubscribeReplay.class);
                EventThread observeOnThread = annotation.observeOn();
                EventThread subscribeOnThread = annotation.observeOn();
                SubscribeTag[] tags = annotation.tags();
                int tagLength = tags.length;
                do {
                    String tag = SubscribeTag.DEFAULT;
                    if (tagLength > 0) {
                        tag = tags[tagLength - 1].value();
                    }
                    EventType eventType = new EventType(SubscriptionType.REPLAY, tag, observeOnThread, subscribeOnThread);
                    List<SourceMethod> methodList = annotatedMethods.get(eventType);
                    if (methodList == null) {
                        methodList = new ArrayList<>();
                    }
                    methodList.add(new SourceMethod(method, parameterClazz, listener));
                    annotatedMethods.put(eventType, methodList);
                    tagLength--;
                } while (tagLength > 0);

            } else if (method.isAnnotationPresent(SubscribeBehavior.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeBehavior annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require a single argument.");
                }

                Class<?> parameterClazz = parameterTypes[0];
                if (parameterClazz.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeBehavior annotation on " + parameterClazz
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeBehavior annotation on " + parameterClazz
                            + " but is not 'public'.");
                }

                SubscribeBehavior annotation = method.getAnnotation(SubscribeBehavior.class);
                EventThread observeOnThread = annotation.observeOn();
                EventThread subscribeOnThread = annotation.observeOn();
                SubscribeTag[] tags = annotation.tags();
                int tagLength = tags.length;
                do {
                    String tag = SubscribeTag.DEFAULT;
                    if (tagLength > 0) {
                        tag = tags[tagLength - 1].value();
                    }
                    EventType eventType = new EventType(SubscriptionType.BEHAVIOR, tag, observeOnThread, subscribeOnThread);
                    List<SourceMethod> methodList = annotatedMethods.get(eventType);
                    if (methodList == null) {
                        methodList = new ArrayList<>();
                    }
                    methodList.add(new SourceMethod(method, parameterClazz, listener));
                    annotatedMethods.put(eventType, methodList);
                    tagLength--;
                } while (tagLength > 0);

            }
//            else if (method.isAnnotationPresent(Produce.class)) {
//                Class<?>[] parameterTypes = method.getParameterTypes();
//                if (parameterTypes.length != 0) {
//                    throw new IllegalArgumentException("Method " + method + "has @Produce annotation but requires "
//                            + parameterTypes.length + " arguments.  Methods must require zero arguments.");
//                }
//                if (method.getReturnType() == Void.class) {
//                    throw new IllegalArgumentException("Method " + method
//                            + " has a return type of void.  Must declare a non-void type.");
//                }
//
//                Class<?> parameterClazz = method.getReturnType();
//                if (parameterClazz.isInterface()) {
//                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + parameterClazz
//                            + " which is an interface.  Producers must return a concrete class type.");
//                }
//                if (parameterClazz.equals(Void.TYPE)) {
//                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation but has no return type.");
//                }
//
//                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
//                    throw new IllegalArgumentException("Method " + method + " has @Produce annotation on " + parameterClazz
//                            + " but is not 'public'.");
//                }
//
//                Produce annotation = method.getAnnotation(Produce.class);
//                EventThread thread = annotation.thread();
//                Tag[] tags = annotation.tags();
//                int tagLength = (tags == null ? 0 : tags.length);
//                do {
//                    String tag = Tag.DEFAULT;
//                    if (tagLength > 0) {
//                        tag = tags[tagLength - 1].value();
//                    }
//                    EventType eventType = new EventType(SubscriptionType.NONE, parameterClazz, tag);
//                    if (producerMethods.containsKey(eventType)) {
//                        throw new IllegalArgumentException("Producer for type " + eventType + " has already been registered.");
//                    }
//                    producerMethods.put(eventType, new SourceMethod(thread, method, SubscriptionType.NONE));
//                    tagLength--;
//                } while (tagLength > 0);
//            }
        }

        return annotatedMethods;









//        if (!annotatedMethods.isEmpty()) {
//            for (EventType eventType : annotatedMethods.keySet()) {
//                List<SourceMethod> methodList = annotatedMethods.get(eventType);
//                SubscriberEvent subscriberEvent = new SubscriberEvent(
//                        listener, methodList, eventType.observeOnThread, eventType.subscribeOnThread
//                );
//                subscriberMap.put(eventType, subscriberEvent);
//            }
//        }
//
//        return subscriberMap;
    }
}
