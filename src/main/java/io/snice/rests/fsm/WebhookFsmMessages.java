package io.snice.rests.fsm;

public interface WebhookFsmMessages {

    record Start() implements WebhookFsmMessages {};

    record WakeUp() implements WebhookFsmMessages {};

}
