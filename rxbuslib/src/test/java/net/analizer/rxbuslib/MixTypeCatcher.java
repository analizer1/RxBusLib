package net.analizer.rxbuslib;

import junit.framework.Assert;

import net.analizer.rxbuslib.annotations.Subscribe;
import net.analizer.rxbuslib.annotations.SubscribeTag;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple SubscriberEvent mock that records Strings.
 * <p/>
 * For testing fun, also includes a landmine method that Bus tests are
 * required <em>not</em> to call ({@link #methodWithoutAnnotation(String)}).
 */
public class MixTypeCatcher {
    public List<String> eventString = new ArrayList<>();
    public List<String> eventStringWithCustomTag = new ArrayList<>();
    public List<Integer> eventInt = new ArrayList<>();
    public List<Integer> eventIntWithCustomTag = new ArrayList<>();

    @Subscribe
    public void subscribeString(String string) {
        eventString.add(string);
    }

    @Subscribe(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeStringWithTag(String string) {
        eventStringWithCustomTag.add(string);
    }

    @Subscribe
    public void subscribeInt(int anInt) {
        eventInt.add(anInt);
    }

    @Subscribe(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeIntWithTag(int anInt) {
        eventIntWithCustomTag.add(anInt);
    }

    public void methodWithoutAnnotation(String string) {
        Assert.fail("Event bus must not call methods without @Subscribe!");
    }
}
