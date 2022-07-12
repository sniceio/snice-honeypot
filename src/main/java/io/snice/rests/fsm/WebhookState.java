package io.snice.rests.fsm;

public enum WebhookState {
    INIT, SEND, WAIT, SLEEP, TERMINATED;
}
