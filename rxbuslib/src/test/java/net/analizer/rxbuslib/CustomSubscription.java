package net.analizer.rxbuslib;

import net.analizer.rxbuslib.annotations.SubscribeBehavior;
import net.analizer.rxbuslib.annotations.SubscribeTag;

public class CustomSubscription {
    public Integer cartAmount = 0;

    @SubscribeBehavior(
            tags = {@SubscribeTag("cart")}
    )
    public void onCartUpdated(Integer cartAmount) {
        this.cartAmount = cartAmount;
    }
}
