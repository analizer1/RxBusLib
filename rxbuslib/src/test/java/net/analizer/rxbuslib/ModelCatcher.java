package net.analizer.rxbuslib;

import junit.framework.Assert;

import net.analizer.rxbuslib.annotations.Subscribe;
import net.analizer.rxbuslib.annotations.SubscribeBehavior;
import net.analizer.rxbuslib.annotations.SubscribeTag;

/**
 * A simple SubscriberEvent mock that records Strings.
 * <p/>
 * For testing fun, also includes a landmine method that Bus tests are
 * required <em>not</em> to call ({@link #methodWithoutAnnotation(String)}).
 */
public class ModelCatcher {
    Model model;
    Model behaviorModel;

    @Subscribe
    public void subscribeModel(Model model) {
        this.model = model;
    }

    @Subscribe(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void hereHaveAStringWithTag(Model model) {
        this.model = model;
    }

    @SubscribeBehavior
    public void subscribeBehaviorModel(Model model) {
        this.behaviorModel = model;
    }

    @SubscribeBehavior(
            tags = {@SubscribeTag("NotDefaultTag")}
    )
    public void subscribeBehaviorModelWithTag(Model model) {
        this.behaviorModel = model;
    }

    public void methodWithoutAnnotation(String string) {
        Assert.fail("Event bus must not call methods without @Subscribe!");
    }
}
