package io.snice.rests.fsm;

import io.hektor.fsm.Context;

import java.time.Duration;

public class WebhookFsmContext implements Context {

    private final static WebhookFsmMessages.WakeUp WAKE_UP = new WebhookFsmMessages.WakeUp();

    public void sleep(Duration duration) {
        getScheduler().schedule(WAKE_UP, duration);
    }
}
