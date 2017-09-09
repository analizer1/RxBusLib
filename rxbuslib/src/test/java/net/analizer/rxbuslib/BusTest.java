package net.analizer.rxbuslib;

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

import java.util.Collection;
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
    private static final String BUS_IDENTIFIER = "test-bus";

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

//    @Test
//    public void registerPostUnSubscribe() {
//        StringCatcher catcher = new StringCatcher();
//        bus.register(catcher);
//        bus.postPublish("test");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        assertEquals(1, catcher.events.size());
//        assertEquals(0, catcher.eventTagA.size());
//        assertEquals("test", catcher.events.get(0));
//        bus.unRegister(catcher);
//
//        catcher.events.clear();
//        bus.postPublish("test2");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        assertEquals(0, catcher.events.size());
//        assertEquals(0, catcher.eventTagA.size());
//    }

//    @Test
//    public void basicSubscribeWithTag() {
//        StringCatcher catcher = new StringCatcher();
//        bus.register(catcher);
//
//        bus.postPublish("test");
//        bus.postPublish("testa", "a");
//
//        assertEquals(1, catcher.events.size());
//        assertEquals("test", catcher.events.get(0));
//        assertEquals(1, catcher.eventTagA.size());
//        assertEquals("testa", catcher.eventTagA.get(0));
//    }

//    @Test
//    public void ignoreSyntheticBridgeMethods() {
//        SubscriberImpl catcher = new SubscriberImpl();
//        bus.register(catcher);
//        bus.post(EVENT);
//    }

}
