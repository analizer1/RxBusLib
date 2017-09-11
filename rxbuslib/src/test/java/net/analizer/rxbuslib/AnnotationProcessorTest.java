package net.analizer.rxbuslib;

import net.analizer.rxbuslib.annotations.AnnotationProcessor;
import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.annotations.SubscribeTag;
import net.analizer.rxbuslib.annotations.SubscriptionAnnotationProcessor;
import net.analizer.rxbuslib.annotations.SubscriptionType;
import net.analizer.rxbuslib.events.EventType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Test case for {@link net.analizer.rxbuslib.annotations.AnnotationProcessor}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class AnnotationProcessorTest {

    private AnnotationProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new SubscriptionAnnotationProcessor();
    }

    @Test
    public void testFindAnnotatedMethods() {
        StringCatcher catcher1 = new StringCatcher();
        Map<EventType, List<SourceMethod>> allSubscribers = processor.findAllSubscribers(catcher1);
        //there should be 2 EventTypes
        assertEquals(2, allSubscribers.size());

        Set<Map.Entry<EventType, List<SourceMethod>>> entries = allSubscribers.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator = entries.iterator();
        Map.Entry<EventType, List<SourceMethod>> next = iterator.next();

        EventType eventType = next.getKey();
        List<SourceMethod> sourceMethodList = next.getValue();
        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(1, sourceMethodList.size());
        assertEquals("hereHaveAString", sourceMethodList.get(0).method.getName());
        assertEquals(String.class, sourceMethodList.get(0).parameterClass);

        next = iterator.next();
        eventType = next.getKey();
        sourceMethodList = next.getValue();
        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(1, sourceMethodList.size());
        assertEquals("hereHaveAStringWithTagA", sourceMethodList.get(0).method.getName());
        assertEquals(String.class, sourceMethodList.get(0).parameterClass);
    }

    @Test
    public void testFindAnnotatedMethodsOfDifferentInstantOfSameTypeObject() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcher catcher2 = new StringCatcher();

        Map<EventType, List<SourceMethod>> allSubscribers1 = processor.findAllSubscribers(catcher1);
        Map<EventType, List<SourceMethod>> allSubscribers2 = processor.findAllSubscribers(catcher2);

        assertEquals(2, allSubscribers1.size());
        assertEquals(2, allSubscribers2.size());

        Set<Map.Entry<EventType, List<SourceMethod>>> entries1 = allSubscribers1.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator1 = entries1.iterator();
        Map.Entry<EventType, List<SourceMethod>> next1 = iterator1.next();

        Set<Map.Entry<EventType, List<SourceMethod>>> entries2 = allSubscribers2.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator2 = entries2.iterator();
        Map.Entry<EventType, List<SourceMethod>> next2 = iterator2.next();

        List<SourceMethod> sourceMethodList1 = next1.getValue();
        List<SourceMethod> sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        SourceMethod sourceMethod1 = sourceMethodList1.get(0);
        SourceMethod sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAString", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isNotEqualTo(sourceMethod2);

        // test second method

        next1 = iterator1.next();
        next2 = iterator2.next();

        sourceMethodList1 = next1.getValue();
        sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        sourceMethod1 = sourceMethodList1.get(0);
        sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAStringWithTagA", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isNotEqualTo(sourceMethod2);
    }

    @Test
    public void testFindAnnotatedMethodsOfSameInstantOfSameTypeObject() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcher catcher2 = catcher1;

        Map<EventType, List<SourceMethod>> allSubscribers1 = processor.findAllSubscribers(catcher1);
        Map<EventType, List<SourceMethod>> allSubscribers2 = processor.findAllSubscribers(catcher2);

        assertEquals(2, allSubscribers1.size());
        assertEquals(2, allSubscribers2.size());

        Set<Map.Entry<EventType, List<SourceMethod>>> entries1 = allSubscribers1.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator1 = entries1.iterator();
        Map.Entry<EventType, List<SourceMethod>> next1 = iterator1.next();

        Set<Map.Entry<EventType, List<SourceMethod>>> entries2 = allSubscribers2.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator2 = entries2.iterator();
        Map.Entry<EventType, List<SourceMethod>> next2 = iterator2.next();

        List<SourceMethod> sourceMethodList1 = next1.getValue();
        List<SourceMethod> sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        SourceMethod sourceMethod1 = sourceMethodList1.get(0);
        SourceMethod sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAString", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isEqualTo(sourceMethod2);

        // test second method

        next1 = iterator1.next();
        next2 = iterator2.next();

        sourceMethodList1 = next1.getValue();
        sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        sourceMethod1 = sourceMethodList1.get(0);
        sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAStringWithTagA", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isEqualTo(sourceMethod2);
    }

    @Test
    public void testFindAnnotatedMethodsOfDifferentInstantOfDifferentTypeObject() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcherCopy catcher2 = new StringCatcherCopy();

        Map<EventType, List<SourceMethod>> allSubscribers1 = processor.findAllSubscribers(catcher1);
        Map<EventType, List<SourceMethod>> allSubscribers2 = processor.findAllSubscribers(catcher2);

        assertEquals(2, allSubscribers1.size());
        assertEquals(2, allSubscribers2.size());

        Set<Map.Entry<EventType, List<SourceMethod>>> entries1 = allSubscribers1.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator1 = entries1.iterator();
        Map.Entry<EventType, List<SourceMethod>> next1 = iterator1.next();

        Set<Map.Entry<EventType, List<SourceMethod>>> entries2 = allSubscribers2.entrySet();
        Iterator<Map.Entry<EventType, List<SourceMethod>>> iterator2 = entries2.iterator();
        Map.Entry<EventType, List<SourceMethod>> next2 = iterator2.next();

        List<SourceMethod> sourceMethodList1 = next1.getValue();
        List<SourceMethod> sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        SourceMethod sourceMethod1 = sourceMethodList1.get(0);
        SourceMethod sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAString", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isNotEqualTo(sourceMethod2);

        // test second method

        next1 = iterator1.next();
        next2 = iterator2.next();

        sourceMethodList1 = next1.getValue();
        sourceMethodList2 = next2.getValue();
        assertEquals(1, sourceMethodList1.size());
        assertEquals(1, sourceMethodList2.size());

        sourceMethod1 = sourceMethodList1.get(0);
        sourceMethod2 = sourceMethodList2.get(0);
        assertEquals(sourceMethod1.method.getName(), sourceMethod2.method.getName());
        assertEquals(sourceMethod1.parameterClass, sourceMethod2.parameterClass);
        assertEquals(String.class, sourceMethod1.parameterClass);
        assertEquals("hereHaveAStringWithTagA", sourceMethod1.method.getName());

        assertThat(sourceMethod1).isNotEqualTo(sourceMethod2);
    }

    @Test
    public void testObjectUniqueInstanceId() {
        StringCatcher catcher1 = new StringCatcher();
        int id1 = System.identityHashCode(catcher1);

        StringCatcher catcher2 = new StringCatcher();
        int id2 = System.identityHashCode(catcher2);

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isEqualTo(System.identityHashCode(catcher1));
    }
}
