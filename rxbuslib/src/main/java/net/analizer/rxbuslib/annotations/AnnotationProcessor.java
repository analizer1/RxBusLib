package net.analizer.rxbuslib.annotations;

import android.support.annotation.NonNull;

import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.events.SubscriberEvent;

import java.util.List;
import java.util.Map;

public interface AnnotationProcessor {
//    Map<EventType, ProducerEvent> findAllProducers(@NonNull Object listener);

    Map<EventType, List<SourceMethod>> findAllSubscribers(@NonNull Object listener);
}
