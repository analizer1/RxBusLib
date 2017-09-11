package net.analizer.rxbuslib;

import junit.framework.Assert;

import net.analizer.rxbuslib.annotations.Subscribe;
import net.analizer.rxbuslib.annotations.SubscribeBehavior;
import net.analizer.rxbuslib.annotations.SubscribeReplay;
import net.analizer.rxbuslib.annotations.SubscribeTag;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple SubscriberEvent mock that records Strings.
 * <p/>
 * For testing fun, also includes a landmine method that Bus tests are
 * required <em>not</em> to call ({@link #methodWithoutAnnotation(String)}).
 */
public class MixTypeCatcherBehavior {
    public List<String> eventString = new ArrayList<>();
    public List<String> eventStringWithCustomTag = new ArrayList<>();
    public List<String> eventStringReplay = new ArrayList<>();
    public List<String> eventStringReplayWithCustomTag = new ArrayList<>();
    public List<Integer> eventInt = new ArrayList<>();
    public List<Integer> eventIntWithCustomTag = new ArrayList<>();
    public List<Integer> eventReplayInt = new ArrayList<>();
    public List<Integer> eventIntReplayWithCustomTag = new ArrayList<>();
    public List<Double> eventBehaviorDouble = new ArrayList<>();
    public List<Double> eventDoubleBehaviorWithCustomTag = new ArrayList<>();

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

    @SubscribeReplay
    public void subscribeStringReplay(String string) {
        eventStringReplay.add(string);
    }

    @SubscribeReplay(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeStringReplayWithTag(String string) {
        eventStringReplayWithCustomTag.add(string);
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

    @SubscribeReplay
    public void subscribeIntReplay(int anInt) {
        eventReplayInt.add(anInt);
    }

    @SubscribeReplay(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeIntReplayWithTag(int anInt) {
        eventIntReplayWithCustomTag.add(anInt);
    }

    @SubscribeBehavior
    public void subscribeDoubleBehavior(double anDouble) {
        eventBehaviorDouble.add(anDouble);
    }

    @SubscribeBehavior(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeDoubleBehaviorWithTag(double anDouble) {
        eventDoubleBehaviorWithCustomTag.add(anDouble);
    }

    public void methodWithoutAnnotation(String string) {
        Assert.fail("Event bus must not call methods without @Subscribe!");
    }
}
