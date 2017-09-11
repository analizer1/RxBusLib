package net.analizer.rxbuslib;

import net.analizer.rxbuslib.annotations.SourceMethod;
import net.analizer.rxbuslib.annotations.SubscribeTag;
import net.analizer.rxbuslib.annotations.SubscriptionType;
import net.analizer.rxbuslib.events.EventType;
import net.analizer.rxbuslib.events.SubscriberEvent;
import net.analizer.rxbuslib.interfaces.Bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static junit.framework.Assert.assertEquals;

/**
 * Test case for {@link Bus}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class BusTest {

    private RxBus bus;

    @Before
    public void setUp() throws Exception {
//        bus = new RxBus(ThreadEnforcer.ANY, BUS_IDENTIFIER);
        bus = new RxBus();
    }

    @Test
    public void testRegister() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions =
                bus.getSubscriptions();

        //there should be 2 EventTypes
        assertEquals(2, subscriptions.size());

        Set<Map.Entry<EventType, SubscriberEvent>> entries = subscriptions.entrySet();
        Iterator<Map.Entry<EventType, SubscriberEvent>> iterator = entries.iterator();
        Map.Entry<EventType, SubscriberEvent> next = iterator.next();

        EventType eventType = next.getKey();
        SubscriberEvent subscriberEvent = next.getValue();
        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        assertEquals("hereHaveAString", subscriberEvent.getMethodList().get(0).method.getName());
        assertEquals(String.class, subscriberEvent.getMethodList().get(0).parameterClass);

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();
        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        assertEquals("hereHaveAStringWithTagA", subscriberEvent.getMethodList().get(0).method.getName());
        assertEquals(String.class, subscriberEvent.getMethodList().get(0).parameterClass);
    }

    @Test
    public void testUnRegisterAfterRegister() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        bus.unRegister(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();

        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testRegisterNull() {
        StringCatcher catcher = null;
        bus.register(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testUnRegisterNull() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());

        bus.unRegister(null);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
    }

    @Test
    public void testUnRegisterNewObjectOfSameTypeThatIsNotRegistered() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());

        StringCatcher catcher2 = new StringCatcher();
        bus.unRegister(catcher2);

        subscriptions = bus.getSubscriptions();
        //should remain the same
        assertEquals(2, subscriptions.size());
    }

    @Test
    public void testRegisterDifferentInstanceOfSameType() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());

        StringCatcher catcher2 = new StringCatcher();
        bus.unRegister(catcher2);

        subscriptions = bus.getSubscriptions();
        //should remain the same
        assertEquals(2, subscriptions.size());

        Set<Map.Entry<EventType, SubscriberEvent>> entries = subscriptions.entrySet();
        Iterator<Map.Entry<EventType, SubscriberEvent>> iterator = entries.iterator();
        Map.Entry<EventType, SubscriberEvent> next = iterator.next();
        //the first method count should be 1
        SubscriberEvent subscriberEvent = next.getValue();
        assertEquals(1, subscriberEvent.getMethodList().size());
        //the second method count should be 1
        subscriberEvent = next.getValue();
        assertEquals(1, subscriberEvent.getMethodList().size());

        bus.register(catcher2);
        subscriptions = bus.getSubscriptions();
        //the subscription should remain the same
        assertEquals(2, subscriptions.size());
        //instead, the method count should increase
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        subscriberEvent = next.getValue();
        //here the first method count should increase by 1
        assertEquals(2, subscriberEvent.getMethodList().size());

        next = iterator.next();
        subscriberEvent = next.getValue();
        //here the second method count should increase by 1
        assertEquals(2, subscriberEvent.getMethodList().size());
    }

    @Test
    public void testRegisterPostUnSubscribe() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        bus.postPublish("test");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, catcher.events.size());
        assertEquals(0, catcher.eventWithCustomTag.size());
        assertEquals("test", catcher.events.get(0));
        bus.unRegister(catcher);

        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testPostAfterUnsubsidised() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);
        bus.postPublish("test");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(1, catcher.events.size());
        assertEquals(0, catcher.eventWithCustomTag.size());
        assertEquals("test", catcher.events.get(0));
        bus.unRegister(catcher);

        ConcurrentMap<EventType, SubscriberEvent> subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());

        catcher.events.clear();
        bus.postPublish("test2");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, catcher.events.size());
        assertEquals(0, catcher.eventWithCustomTag.size());
    }

    @Test
    public void testMultipleMixRegisters() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcher catcher2 = new StringCatcher();
        StringCatcherCopy catcherCopy = new StringCatcherCopy();
        MixTypeCatcher catcherAnother = new MixTypeCatcher();

        int catcher1InstanceId = System.identityHashCode(catcher1);
        int catcher2InstanceId = System.identityHashCode(catcher2);
        int catcherCopyInstanceId = System.identityHashCode(catcherCopy);
        int catcherAnotherInstanceId = System.identityHashCode(catcherAnother);

        bus.register(catcher1);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions;
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());

        Set<Map.Entry<EventType, SubscriberEvent>> entries;
        Iterator<Map.Entry<EventType, SubscriberEvent>> iterator;
        Map.Entry<EventType, SubscriberEvent> next;
        EventType eventType;
        SubscriberEvent subscriberEvent;
        SourceMethod sourceMethod;

        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        sourceMethod = subscriberEvent.getMethodList().get(0);
        assertEquals(String.class, sourceMethod.parameterClass);
        assertEquals("hereHaveAString", sourceMethod.method.getName());
        assertEquals(catcher1InstanceId, sourceMethod.instanceId);

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        sourceMethod = subscriberEvent.getMethodList().get(0);
        assertEquals(String.class, sourceMethod.parameterClass);
        assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
        assertEquals(catcher1InstanceId, sourceMethod.instanceId);

        bus.register(catcher2);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(2, subscriberEvent.getMethodList().size());

        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAString", sourceMethod.method.getName());
            assertEquals(i < 1 ? catcher1InstanceId : catcher2InstanceId, sourceMethod.instanceId);
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(2, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
            assertEquals(i < 1 ? catcher1InstanceId : catcher2InstanceId, sourceMethod.instanceId);
        }

        bus.register(catcherCopy);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(3, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAString", sourceMethod.method.getName());

            int instanceId;
            switch (i) {
                case 0:
                    instanceId = catcher1InstanceId;
                    break;

                case 1:
                    instanceId = catcher2InstanceId;
                    break;

                default:
                    instanceId = catcherCopyInstanceId;
            }
            assertEquals(instanceId, sourceMethod.instanceId);
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(3, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());

            int instanceId;
            switch (i) {
                case 0:
                    instanceId = catcher1InstanceId;
                    break;

                case 1:
                    instanceId = catcher2InstanceId;
                    break;

                default:
                    instanceId = catcherCopyInstanceId;
            }
            assertEquals(instanceId, sourceMethod.instanceId);
        }

        bus.register(catcherAnother);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(5, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher1InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 2:
                    assertEquals(catcherCopyInstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 3:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeString", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 4:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeInt", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(5, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher1InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 2:
                    assertEquals(catcherCopyInstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 3:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeStringWithTag", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 4:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeIntWithTag", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }
    }

    @Test
    public void testMultipleRegistersAndUnRegisters() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcher catcher2 = new StringCatcher();
        StringCatcherCopy catcherCopy = new StringCatcherCopy();
        MixTypeCatcher catcherAnother = new MixTypeCatcher();

        int catcher1InstanceId = System.identityHashCode(catcher1);
        int catcher2InstanceId = System.identityHashCode(catcher2);
        int catcherCopyInstanceId = System.identityHashCode(catcherCopy);
        int catcherAnotherInstanceId = System.identityHashCode(catcherAnother);

        bus.register(catcher1);
        ConcurrentMap<EventType, SubscriberEvent> subscriptions;
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());

        Set<Map.Entry<EventType, SubscriberEvent>> entries;
        Iterator<Map.Entry<EventType, SubscriberEvent>> iterator;
        Map.Entry<EventType, SubscriberEvent> next;
        EventType eventType;
        SubscriberEvent subscriberEvent;
        SourceMethod sourceMethod;

        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        sourceMethod = subscriberEvent.getMethodList().get(0);
        assertEquals(String.class, sourceMethod.parameterClass);
        assertEquals("hereHaveAString", sourceMethod.method.getName());
        assertEquals(catcher1InstanceId, sourceMethod.instanceId);

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(1, subscriberEvent.getMethodList().size());
        sourceMethod = subscriberEvent.getMethodList().get(0);
        assertEquals(String.class, sourceMethod.parameterClass);
        assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
        assertEquals(catcher1InstanceId, sourceMethod.instanceId);

        bus.postPublish("subscribe1");
        bus.postPublish("subscribe1 with tag", "NotDefaultTag");
        assertEquals(1, catcher1.events.size());
        assertEquals(1, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe1", catcher1.events.get(0));
        assertEquals("subscribe1 with tag", catcher1.eventWithCustomTag.get(0));

        // register catcher 2
        bus.register(catcher2);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(2, subscriberEvent.getMethodList().size());

        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAString", sourceMethod.method.getName());
            assertEquals(i < 1 ? catcher1InstanceId : catcher2InstanceId, sourceMethod.instanceId);
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(2, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
            assertEquals(i < 1 ? catcher1InstanceId : catcher2InstanceId, sourceMethod.instanceId);
        }

        bus.postPublish("subscribe2");
        bus.postPublish("subscribe2 with tag", "NotDefaultTag");
        assertEquals(2, catcher1.events.size());
        assertEquals(2, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe2", catcher1.events.get(1));
        assertEquals("subscribe2 with tag", catcher1.eventWithCustomTag.get(1));

        assertEquals(1, catcher2.events.size());
        assertEquals(1, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe2", catcher2.events.get(0));
        assertEquals("subscribe2 with tag", catcher2.eventWithCustomTag.get(0));

        // register catcher copy
        bus.register(catcherCopy);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(3, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAString", sourceMethod.method.getName());

            int instanceId;
            switch (i) {
                case 0:
                    instanceId = catcher1InstanceId;
                    break;

                case 1:
                    instanceId = catcher2InstanceId;
                    break;

                default:
                    instanceId = catcherCopyInstanceId;
            }
            assertEquals(instanceId, sourceMethod.instanceId);
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(3, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);
            assertEquals(String.class, sourceMethod.parameterClass);
            assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());

            int instanceId;
            switch (i) {
                case 0:
                    instanceId = catcher1InstanceId;
                    break;

                case 1:
                    instanceId = catcher2InstanceId;
                    break;

                default:
                    instanceId = catcherCopyInstanceId;
            }
            assertEquals(instanceId, sourceMethod.instanceId);
        }

        bus.postPublish("subscribe3");
        bus.postPublish("subscribe3 with tag", "NotDefaultTag");
        assertEquals(3, catcher1.events.size());
        assertEquals(3, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe3", catcher1.events.get(2));
        assertEquals("subscribe3 with tag", catcher1.eventWithCustomTag.get(2));

        assertEquals(2, catcher2.events.size());
        assertEquals(2, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe3", catcher2.events.get(1));
        assertEquals("subscribe3 with tag", catcher2.eventWithCustomTag.get(1));

        assertEquals(1, catcherCopy.events.size());
        assertEquals(1, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe3", catcherCopy.events.get(0));
        assertEquals("subscribe3 with tag", catcherCopy.eventWithCustomTag.get(0));

        // register catcher another
        bus.register(catcherAnother);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(5, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher1InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 2:
                    assertEquals(catcherCopyInstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 3:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeString", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 4:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeInt", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals("NotDefaultTag", eventType.tag);
        assertEquals(5, subscriberEvent.getMethodList().size());
        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher1InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 2:
                    assertEquals(catcherCopyInstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAStringWithTagA", sourceMethod.method.getName());
                    break;

                case 3:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeStringWithTag", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 4:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeIntWithTag", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        bus.postPublish("subscribe4");
        bus.postPublish("subscribe4 with tag", "NotDefaultTag");
        bus.postPublish(1);
        bus.postPublish(1, "NotDefaultTag");

        assertEquals(4, catcher1.events.size());
        assertEquals(4, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe4", catcher1.events.get(3));
        assertEquals("subscribe4 with tag", catcher1.eventWithCustomTag.get(3));

        assertEquals(3, catcher2.events.size());
        assertEquals(3, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe4", catcher2.events.get(2));
        assertEquals("subscribe4 with tag", catcher2.eventWithCustomTag.get(2));

        assertEquals(2, catcherCopy.events.size());
        assertEquals(2, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe4", catcherCopy.events.get(1));
        assertEquals("subscribe4 with tag", catcherCopy.eventWithCustomTag.get(1));

        assertEquals(1, catcherAnother.eventString.size());
        assertEquals(1, catcherAnother.eventStringWithCustomTag.size());
        assertEquals("subscribe4", catcherAnother.eventString.get(0));
        assertEquals("subscribe4 with tag", catcherAnother.eventStringWithCustomTag.get(0));

        assertEquals(1, catcherAnother.eventIntWithCustomTag.size());
        assertEquals(1, catcherAnother.eventInt.size());
        assertEquals(Integer.valueOf(1), catcherAnother.eventInt.get(0));
        assertEquals(Integer.valueOf(1), catcherAnother.eventIntWithCustomTag.get(0));

        // now unregister catcherCopy
        bus.unRegister(catcherCopy);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(4, subscriberEvent.getMethodList().size());

        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher1InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 2:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeString", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 3:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeInt", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        bus.postPublish("subscribe5");
        bus.postPublish("subscribe5 with tag", "NotDefaultTag");
        bus.postPublish(2);
        bus.postPublish(2, "NotDefaultTag");

        assertEquals(5, catcher1.events.size());
        assertEquals(5, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe5", catcher1.events.get(4));
        assertEquals("subscribe5 with tag", catcher1.eventWithCustomTag.get(4));

        assertEquals(4, catcher2.events.size());
        assertEquals(4, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe5", catcher2.events.get(3));
        assertEquals("subscribe5 with tag", catcher2.eventWithCustomTag.get(3));

        assertEquals(2, catcherCopy.events.size());
        assertEquals(2, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe4", catcherCopy.events.get(1));
        assertEquals("subscribe4 with tag", catcherCopy.eventWithCustomTag.get(1));

        assertEquals(2, catcherAnother.eventString.size());
        assertEquals(2, catcherAnother.eventStringWithCustomTag.size());
        assertEquals("subscribe5", catcherAnother.eventString.get(1));
        assertEquals("subscribe5 with tag", catcherAnother.eventStringWithCustomTag.get(1));

        assertEquals(2, catcherAnother.eventInt.size());
        assertEquals(2, catcherAnother.eventIntWithCustomTag.size());
        assertEquals(2, (int) catcherAnother.eventInt.get(1));
        assertEquals(2, (int) catcherAnother.eventIntWithCustomTag.get(1));

        // unregister catcher 1
        bus.unRegister(catcher1);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(3, subscriberEvent.getMethodList().size());

        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(catcher2InstanceId, sourceMethod.instanceId);
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("hereHaveAString", sourceMethod.method.getName());
                    break;

                case 1:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeString", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 2:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeInt", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        bus.postPublish("subscribe6");
        bus.postPublish("subscribe6 with tag", "NotDefaultTag");
        bus.postPublish(3);
        bus.postPublish(3, "NotDefaultTag");

        assertEquals(5, catcher1.events.size());
        assertEquals(5, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe5", catcher1.events.get(4));
        assertEquals("subscribe5 with tag", catcher1.eventWithCustomTag.get(4));

        assertEquals(5, catcher2.events.size());
        assertEquals(5, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe6", catcher2.events.get(4));
        assertEquals("subscribe6 with tag", catcher2.eventWithCustomTag.get(4));

        assertEquals(2, catcherCopy.events.size());
        assertEquals(2, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe4", catcherCopy.events.get(1));
        assertEquals("subscribe4 with tag", catcherCopy.eventWithCustomTag.get(1));

        assertEquals(3, catcherAnother.eventString.size());
        assertEquals(3, catcherAnother.eventStringWithCustomTag.size());
        assertEquals("subscribe6", catcherAnother.eventString.get(2));
        assertEquals("subscribe6 with tag", catcherAnother.eventStringWithCustomTag.get(2));

        assertEquals(3, catcherAnother.eventInt.size());
        assertEquals(3, catcherAnother.eventIntWithCustomTag.size());
        assertEquals(3, (int) catcherAnother.eventInt.get(2));
        assertEquals(3, (int) catcherAnother.eventIntWithCustomTag.get(2));

        // unregister catcher 2
        bus.unRegister(catcher2);
        subscriptions = bus.getSubscriptions();
        assertEquals(2, subscriptions.size());
        entries = subscriptions.entrySet();
        iterator = entries.iterator();
        next = iterator.next();
        eventType = next.getKey();
        subscriberEvent = next.getValue();

        assertEquals(SubscriptionType.PUBLISH, eventType.subscriptionType);
        assertEquals(SubscribeTag.DEFAULT, eventType.tag);
        assertEquals(2, subscriberEvent.getMethodList().size());

        for (int i = 0; i < subscriberEvent.getMethodList().size(); i++) {
            sourceMethod = subscriberEvent.getMethodList().get(i);

            switch (i) {
                case 0:
                    assertEquals(String.class, sourceMethod.parameterClass);
                    assertEquals("subscribeString", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;

                case 1:
                    assertEquals(int.class, sourceMethod.parameterClass);
                    assertEquals("subscribeInt", sourceMethod.method.getName());
                    assertEquals(catcherAnotherInstanceId, sourceMethod.instanceId);
                    break;
            }
        }

        bus.postPublish("subscribe7");
        bus.postPublish("subscribe7 with tag", "NotDefaultTag");
        bus.postPublish(4);
        bus.postPublish(4, "NotDefaultTag");

        assertEquals(5, catcher1.events.size());
        assertEquals(5, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe5", catcher1.events.get(4));
        assertEquals("subscribe5 with tag", catcher1.eventWithCustomTag.get(4));

        assertEquals(5, catcher2.events.size());
        assertEquals(5, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe6", catcher2.events.get(4));
        assertEquals("subscribe6 with tag", catcher2.eventWithCustomTag.get(4));

        assertEquals(2, catcherCopy.events.size());
        assertEquals(2, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe4", catcherCopy.events.get(1));
        assertEquals("subscribe4 with tag", catcherCopy.eventWithCustomTag.get(1));

        assertEquals(4, catcherAnother.eventString.size());
        assertEquals(4, catcherAnother.eventStringWithCustomTag.size());
        assertEquals("subscribe7", catcherAnother.eventString.get(3));
        assertEquals("subscribe7 with tag", catcherAnother.eventStringWithCustomTag.get(3));

        assertEquals(4, catcherAnother.eventInt.size());
        assertEquals(4, catcherAnother.eventIntWithCustomTag.size());
        assertEquals(4, (int) catcherAnother.eventInt.get(3));
        assertEquals(4, (int) catcherAnother.eventIntWithCustomTag.get(3));

        // unregister catcher AnotherStringCatcher
        bus.unRegister(catcherAnother);
        subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());

        bus.postPublish("subscribe8");
        bus.postPublish("subscribe8 with tag", "NotDefaultTag");
        bus.postPublish(5);
        bus.postPublish(5, "NotDefaultTag");

        assertEquals(5, catcher1.events.size());
        assertEquals(5, catcher1.eventWithCustomTag.size());
        assertEquals("subscribe5", catcher1.events.get(4));
        assertEquals("subscribe5 with tag", catcher1.eventWithCustomTag.get(4));

        assertEquals(5, catcher2.events.size());
        assertEquals(5, catcher2.eventWithCustomTag.size());
        assertEquals("subscribe6", catcher2.events.get(4));
        assertEquals("subscribe6 with tag", catcher2.eventWithCustomTag.get(4));

        assertEquals(2, catcherCopy.events.size());
        assertEquals(2, catcherCopy.eventWithCustomTag.size());
        assertEquals("subscribe4", catcherCopy.events.get(1));
        assertEquals("subscribe4 with tag", catcherCopy.eventWithCustomTag.get(1));

        assertEquals(4, catcherAnother.eventString.size());
        assertEquals(4, catcherAnother.eventStringWithCustomTag.size());
        assertEquals("subscribe7", catcherAnother.eventString.get(3));
        assertEquals("subscribe7 with tag", catcherAnother.eventStringWithCustomTag.get(3));

        assertEquals(4, catcherAnother.eventInt.size());
        assertEquals(4, catcherAnother.eventIntWithCustomTag.size());
        assertEquals(4, (int) catcherAnother.eventInt.get(3));
        assertEquals(4, (int) catcherAnother.eventIntWithCustomTag.get(3));
    }

    @Test
    public void testSubscribeReplayMixType() {
        MixTypeCatcherReplay catcherReplay = new MixTypeCatcherReplay();
        bus.register(catcherReplay);

        ConcurrentMap<EventType, SubscriberEvent> subscriptions =
                bus.getSubscriptions();

        //there should be 4 EventTypes
        assertEquals(4, subscriptions.size());

        bus.postPublish("test1");
        bus.postPublish("test2", "NotDefaultTag");
        bus.postReplay("test3");
        bus.postReplay("test4", "NotDefaultTag");
        bus.postPublish(1);
        bus.postPublish(2, "NotDefaultTag");
        bus.postReplay(3);
        bus.postReplay(4, "NotDefaultTag");

        assertEquals(1, catcherReplay.eventString.size());
        assertEquals(1, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(1, catcherReplay.eventStringReplay.size());
        assertEquals(1, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(1, catcherReplay.eventInt.size());
        assertEquals(1, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(1, catcherReplay.eventReplayInt.size());
        assertEquals(1, catcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test1", catcherReplay.eventString.get(0));
        assertEquals("test2", catcherReplay.eventStringWithCustomTag.get(0));
        assertEquals("test3", catcherReplay.eventStringReplay.get(0));
        assertEquals("test4", catcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals(1, (int) catcherReplay.eventInt.get(0));
        assertEquals(2, (int) catcherReplay.eventIntWithCustomTag.get(0));
        assertEquals(3, (int) catcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) catcherReplay.eventIntReplayWithCustomTag.get(0));
    }

    @Test
    public void testReplaySubject() {
        MixTypeCatcherReplay catcherReplay = new MixTypeCatcherReplay();
        bus.register(catcherReplay);

        ConcurrentMap<EventType, SubscriberEvent> subscriptions =
                bus.getSubscriptions();

        //there should be 4 EventTypes
        assertEquals(4, subscriptions.size());

        bus.postPublish("test1");
        bus.postPublish("test2", "NotDefaultTag");
        bus.postReplay("test3");
        bus.postReplay("test4", "NotDefaultTag");
        bus.postPublish(1);
        bus.postPublish(2, "NotDefaultTag");
        bus.postReplay(3);
        bus.postReplay(4, "NotDefaultTag");

        assertEquals(1, catcherReplay.eventString.size());
        assertEquals(1, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(1, catcherReplay.eventStringReplay.size());
        assertEquals(1, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(1, catcherReplay.eventInt.size());
        assertEquals(1, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(1, catcherReplay.eventReplayInt.size());
        assertEquals(1, catcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test1", catcherReplay.eventString.get(0));
        assertEquals("test2", catcherReplay.eventStringWithCustomTag.get(0));
        assertEquals("test3", catcherReplay.eventStringReplay.get(0));
        assertEquals("test4", catcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals(1, (int) catcherReplay.eventInt.get(0));
        assertEquals(2, (int) catcherReplay.eventIntWithCustomTag.get(0));
        assertEquals(3, (int) catcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) catcherReplay.eventIntReplayWithCustomTag.get(0));

        bus.postPublish("test5");
        bus.postPublish("test6", "NotDefaultTag");
        bus.postReplay("test7");
        bus.postReplay("test8", "NotDefaultTag");
        bus.postPublish(5);
        bus.postPublish(6, "NotDefaultTag");
        bus.postReplay(7);
        bus.postReplay(8, "NotDefaultTag");

        assertEquals(2, catcherReplay.eventString.size());
        assertEquals(2, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, catcherReplay.eventStringReplay.size());
        assertEquals(2, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(2, catcherReplay.eventInt.size());
        assertEquals(2, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, catcherReplay.eventReplayInt.size());
        assertEquals(2, catcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test5", catcherReplay.eventString.get(1));
        assertEquals("test6", catcherReplay.eventStringWithCustomTag.get(1));
        assertEquals("test7", catcherReplay.eventStringReplay.get(1));
        assertEquals("test8", catcherReplay.eventStringReplayWithCustomTag.get(1));
        assertEquals(5, (int) catcherReplay.eventInt.get(1));
        assertEquals(6, (int) catcherReplay.eventIntWithCustomTag.get(1));
        assertEquals(7, (int) catcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) catcherReplay.eventIntReplayWithCustomTag.get(1));

        MixTypeCatcherReplay newCatcherReplay = new MixTypeCatcherReplay();
        bus.register(newCatcherReplay);
        subscriptions = bus.getSubscriptions();

        //there should be the same 4 EventTypes
        assertEquals(4, subscriptions.size());
        assertEquals(0, newCatcherReplay.eventString.size());
        assertEquals(0, newCatcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventStringReplay.size());
        assertEquals(2, newCatcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(0, newCatcherReplay.eventInt.size());
        assertEquals(0, newCatcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventReplayInt.size());
        assertEquals(2, newCatcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test3", newCatcherReplay.eventStringReplay.get(0));
        assertEquals("test4", newCatcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals("test7", newCatcherReplay.eventStringReplay.get(1));
        assertEquals("test8", newCatcherReplay.eventStringReplayWithCustomTag.get(1));

        assertEquals(3, (int) newCatcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(0));
        assertEquals(7, (int) newCatcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(1));

        bus.unRegister(newCatcherReplay);
        bus.postPublish("test9");
        bus.postPublish("test10", "NotDefaultTag");
        bus.postReplay("test11");
        bus.postReplay("test12", "NotDefaultTag");
        bus.postPublish(9);
        bus.postPublish(10, "NotDefaultTag");
        bus.postReplay(11);
        bus.postReplay(12, "NotDefaultTag");

        assertEquals(4, subscriptions.size());
        assertEquals(0, newCatcherReplay.eventString.size());
        assertEquals(0, newCatcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventStringReplay.size());
        assertEquals(2, newCatcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(0, newCatcherReplay.eventInt.size());
        assertEquals(0, newCatcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventReplayInt.size());
        assertEquals(2, newCatcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test3", newCatcherReplay.eventStringReplay.get(0));
        assertEquals("test4", newCatcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals("test7", newCatcherReplay.eventStringReplay.get(1));
        assertEquals("test8", newCatcherReplay.eventStringReplayWithCustomTag.get(1));

        assertEquals(3, (int) newCatcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(0));
        assertEquals(7, (int) newCatcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(1));

        assertEquals(3, catcherReplay.eventString.size());
        assertEquals(3, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(3, catcherReplay.eventStringReplay.size());
        assertEquals(3, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(3, catcherReplay.eventInt.size());
        assertEquals(3, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(3, catcherReplay.eventReplayInt.size());
        assertEquals(3, catcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test9", catcherReplay.eventString.get(2));
        assertEquals("test10", catcherReplay.eventStringWithCustomTag.get(2));
        assertEquals("test11", catcherReplay.eventStringReplay.get(2));
        assertEquals("test12", catcherReplay.eventStringReplayWithCustomTag.get(2));
        assertEquals(9, (int) catcherReplay.eventInt.get(2));
        assertEquals(10, (int) catcherReplay.eventIntWithCustomTag.get(2));
        assertEquals(11, (int) catcherReplay.eventReplayInt.get(2));
        assertEquals(12, (int) catcherReplay.eventIntReplayWithCustomTag.get(2));
        bus.unRegister(catcherReplay);
        subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());
    }

    @Test
    public void testBehaviorSubject() {
        MixTypeCatcherBehavior catcherReplay = new MixTypeCatcherBehavior();
        bus.register(catcherReplay);

        ConcurrentMap<EventType, SubscriberEvent> subscriptions =
                bus.getSubscriptions();

        //there should be 6 EventTypes
        assertEquals(6, subscriptions.size());

        bus.postPublish("test1");
        bus.postPublish("test2", "NotDefaultTag");
        bus.postReplay("test3");
        bus.postReplay("test4", "NotDefaultTag");
        bus.postPublish(1);
        bus.postPublish(2, "NotDefaultTag");
        bus.postReplay(3);
        bus.postReplay(4, "NotDefaultTag");
        bus.postBehavior(100.5d);
        bus.postBehavior(101.5d, "NotDefaultTag");

        assertEquals(1, catcherReplay.eventString.size());
        assertEquals(1, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(1, catcherReplay.eventStringReplay.size());
        assertEquals(1, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(1, catcherReplay.eventInt.size());
        assertEquals(1, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(1, catcherReplay.eventReplayInt.size());
        assertEquals(1, catcherReplay.eventIntReplayWithCustomTag.size());
        assertEquals(1, catcherReplay.eventBehaviorDouble.size());
        assertEquals(1, catcherReplay.eventDoubleBehaviorWithCustomTag.size());

        assertEquals("test1", catcherReplay.eventString.get(0));
        assertEquals("test2", catcherReplay.eventStringWithCustomTag.get(0));
        assertEquals("test3", catcherReplay.eventStringReplay.get(0));
        assertEquals("test4", catcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals(1, (int) catcherReplay.eventInt.get(0));
        assertEquals(2, (int) catcherReplay.eventIntWithCustomTag.get(0));
        assertEquals(3, (int) catcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) catcherReplay.eventIntReplayWithCustomTag.get(0));
        assertEquals(100.5d, catcherReplay.eventBehaviorDouble.get(0));
        assertEquals(101.5d, catcherReplay.eventDoubleBehaviorWithCustomTag.get(0));

        bus.postPublish("test5");
        bus.postPublish("test6", "NotDefaultTag");
        bus.postReplay("test7");
        bus.postReplay("test8", "NotDefaultTag");
        bus.postPublish(5);
        bus.postPublish(6, "NotDefaultTag");
        bus.postReplay(7);
        bus.postReplay(8, "NotDefaultTag");
        bus.postBehavior(200.5d);
        bus.postBehavior(201.5d, "NotDefaultTag");

        assertEquals(2, catcherReplay.eventString.size());
        assertEquals(2, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, catcherReplay.eventStringReplay.size());
        assertEquals(2, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(2, catcherReplay.eventInt.size());
        assertEquals(2, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, catcherReplay.eventReplayInt.size());
        assertEquals(2, catcherReplay.eventIntReplayWithCustomTag.size());
        assertEquals(2, catcherReplay.eventBehaviorDouble.size());
        assertEquals(2, catcherReplay.eventDoubleBehaviorWithCustomTag.size());

        assertEquals("test5", catcherReplay.eventString.get(1));
        assertEquals("test6", catcherReplay.eventStringWithCustomTag.get(1));
        assertEquals("test7", catcherReplay.eventStringReplay.get(1));
        assertEquals("test8", catcherReplay.eventStringReplayWithCustomTag.get(1));
        assertEquals(5, (int) catcherReplay.eventInt.get(1));
        assertEquals(6, (int) catcherReplay.eventIntWithCustomTag.get(1));
        assertEquals(7, (int) catcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) catcherReplay.eventIntReplayWithCustomTag.get(1));
        assertEquals(200.5d, catcherReplay.eventBehaviorDouble.get(1));
        assertEquals(201.5d, catcherReplay.eventDoubleBehaviorWithCustomTag.get(1));

        MixTypeCatcherBehavior newCatcherReplay = new MixTypeCatcherBehavior();
        bus.register(newCatcherReplay);
        subscriptions = bus.getSubscriptions();

        //there should be the same 6 EventTypes
        assertEquals(6, subscriptions.size());
        assertEquals(0, newCatcherReplay.eventString.size());
        assertEquals(0, newCatcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventStringReplay.size());
        assertEquals(2, newCatcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(0, newCatcherReplay.eventInt.size());
        assertEquals(0, newCatcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventReplayInt.size());
        assertEquals(2, newCatcherReplay.eventIntReplayWithCustomTag.size());
        assertEquals(1, newCatcherReplay.eventBehaviorDouble.size());
        assertEquals(1, newCatcherReplay.eventDoubleBehaviorWithCustomTag.size());

        assertEquals("test3", newCatcherReplay.eventStringReplay.get(0));
        assertEquals("test4", newCatcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals("test7", newCatcherReplay.eventStringReplay.get(1));
        assertEquals("test8", newCatcherReplay.eventStringReplayWithCustomTag.get(1));

        assertEquals(3, (int) newCatcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(0));
        assertEquals(7, (int) newCatcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(1));

        assertEquals(200.5d, newCatcherReplay.eventBehaviorDouble.get(0));
        assertEquals(201.5d, newCatcherReplay.eventDoubleBehaviorWithCustomTag.get(0));

        bus.unRegister(newCatcherReplay);
        bus.postPublish("test9");
        bus.postPublish("test10", "NotDefaultTag");
        bus.postReplay("test11");
        bus.postReplay("test12", "NotDefaultTag");
        bus.postPublish(9);
        bus.postPublish(10, "NotDefaultTag");
        bus.postReplay(11);
        bus.postReplay(12, "NotDefaultTag");
        bus.postBehavior(300.5d);
        bus.postBehavior(301.5d, "NotDefaultTag");

        //there should be the same 6 EventTypes
        assertEquals(6, subscriptions.size());
        assertEquals(0, newCatcherReplay.eventString.size());
        assertEquals(0, newCatcherReplay.eventStringWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventStringReplay.size());
        assertEquals(2, newCatcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(0, newCatcherReplay.eventInt.size());
        assertEquals(0, newCatcherReplay.eventIntWithCustomTag.size());
        assertEquals(2, newCatcherReplay.eventReplayInt.size());
        assertEquals(2, newCatcherReplay.eventIntReplayWithCustomTag.size());
        assertEquals(1, newCatcherReplay.eventBehaviorDouble.size());
        assertEquals(1, newCatcherReplay.eventDoubleBehaviorWithCustomTag.size());

        assertEquals("test3", newCatcherReplay.eventStringReplay.get(0));
        assertEquals("test4", newCatcherReplay.eventStringReplayWithCustomTag.get(0));
        assertEquals("test7", newCatcherReplay.eventStringReplay.get(1));
        assertEquals("test8", newCatcherReplay.eventStringReplayWithCustomTag.get(1));

        assertEquals(3, (int) newCatcherReplay.eventReplayInt.get(0));
        assertEquals(4, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(0));
        assertEquals(7, (int) newCatcherReplay.eventReplayInt.get(1));
        assertEquals(8, (int) newCatcherReplay.eventIntReplayWithCustomTag.get(1));

        assertEquals(200.5d, newCatcherReplay.eventBehaviorDouble.get(0));
        assertEquals(201.5d, newCatcherReplay.eventDoubleBehaviorWithCustomTag.get(0));

        assertEquals(3, catcherReplay.eventString.size());
        assertEquals(3, catcherReplay.eventStringWithCustomTag.size());
        assertEquals(3, catcherReplay.eventStringReplay.size());
        assertEquals(3, catcherReplay.eventStringReplayWithCustomTag.size());
        assertEquals(3, catcherReplay.eventInt.size());
        assertEquals(3, catcherReplay.eventIntWithCustomTag.size());
        assertEquals(3, catcherReplay.eventReplayInt.size());
        assertEquals(3, catcherReplay.eventIntReplayWithCustomTag.size());

        assertEquals("test9", catcherReplay.eventString.get(2));
        assertEquals("test10", catcherReplay.eventStringWithCustomTag.get(2));
        assertEquals("test11", catcherReplay.eventStringReplay.get(2));
        assertEquals("test12", catcherReplay.eventStringReplayWithCustomTag.get(2));
        assertEquals(9, (int) catcherReplay.eventInt.get(2));
        assertEquals(10, (int) catcherReplay.eventIntWithCustomTag.get(2));
        assertEquals(11, (int) catcherReplay.eventReplayInt.get(2));
        assertEquals(12, (int) catcherReplay.eventIntReplayWithCustomTag.get(2));
        assertEquals(3, catcherReplay.eventBehaviorDouble.size());
        assertEquals(3, catcherReplay.eventDoubleBehaviorWithCustomTag.size());
        assertEquals(200.5d, catcherReplay.eventBehaviorDouble.get(1));
        assertEquals(201.5d, catcherReplay.eventDoubleBehaviorWithCustomTag.get(1));
        assertEquals(300.5d, catcherReplay.eventBehaviorDouble.get(2));
        assertEquals(301.5d, catcherReplay.eventDoubleBehaviorWithCustomTag.get(2));

        bus.unRegister(catcherReplay);
        subscriptions = bus.getSubscriptions();
        assertEquals(0, subscriptions.size());
    }
//    @Test
//    public void ignoreSyntheticBridgeMethods() {
//        SubscriberImpl catcher = new SubscriberImpl();
//        bus.register(catcher);
//        bus.post(EVENT);
//    }

}
