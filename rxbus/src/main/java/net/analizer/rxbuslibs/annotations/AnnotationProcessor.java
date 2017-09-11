package net.analizer.rxbuslibs.annotations;

import android.support.annotation.NonNull;

import net.analizer.rxbuslibs.events.EventType;
import net.analizer.rxbuslibs.events.SubscriberEvent;

import java.util.Map;

public interface AnnotationProcessor {
//    Map<EventType, ProducerEvent> findAllProducers(@NonNull Object listener);

    Map<EventType, SubscriberEvent> findAllSubscribers(@NonNull Object listener);
}
