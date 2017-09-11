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
public class StringCatcher {
    public List<String> events = new ArrayList<>();
    public List<String> eventWithCustomTag = new ArrayList<>();

    @Subscribe
    public void hereHaveAString(String string) {
        events.add(string);
    }

    @Subscribe(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void hereHaveAStringWithTagA(String string) {
        eventWithCustomTag.add(string);
    }

    public void methodWithoutAnnotation(String string) {
        Assert.fail("Event bus must not call methods without @Subscribe!");
    }
}
