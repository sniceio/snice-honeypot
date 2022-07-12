package io.snice.rests.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;

public class WebhookFsm {

    public static final Definition<WebhookState, WebhookFsmContext, WebhookData> definition;

    static {

        final var builder = FSM.of(WebhookState.class).ofContextType(WebhookFsmContext.class).withDataType(WebhookData.class);

        final var init = builder.withInitialState(WebhookState.INIT);
        final var send = builder.withTransientState(WebhookState.SEND);
        final var wait = builder.withState(WebhookState.WAIT);
        final var sleep = builder.withState(WebhookState.SLEEP);

        init.transitionTo(WebhookState.SLEEP)
                .onEvent(WebhookFsmMessages.Start.class)
                .withGuard((evt, ctx, data) -> data.initialDelay().isPresent())
                .withAction((evt, ctx, data) -> ctx.sleep(data.initialDelay().get()));

        // When there is no delay scheduled for the very first webhook
        init.transitionTo(WebhookState.SEND).onEvent(WebhookFsmMessages.Start.class);

        send.transitionTo(WebhookState.WAIT).asDefaultTransition();

        sleep.transitionTo(WebhookState.SEND)
                .onEvent(WebhookFsmMessages.WakeUp.class)
                .withGuard((evt, ctx, data) -> data.moreWebhooksToIssue());

        wait.transitionTo(WebhookState.TERMINATED).onEvent(String.class);


        final var terminated = builder.withFinalState(WebhookState.TERMINATED);

        definition = builder.build();
    }
}
