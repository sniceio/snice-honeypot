package io.snice.rests.core;

import io.hektor.actors.fsm.FsmActor;
import io.hektor.actors.fsm.OnStartFunction;
import io.hektor.core.Hektor;
import io.hektor.core.Props;
import io.snice.functional.Either;
import io.snice.rests.api.ErrorResult;
import io.snice.rests.api.Webhook;
import io.snice.rests.api.WebhookRequest;
import io.snice.rests.fsm.WebhookData;
import io.snice.rests.fsm.WebhookFsm;
import io.snice.rests.fsm.WebhookFsmContext;
import io.snice.rests.fsm.WebhookFsmMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class WebhookManager {

    private static final Logger logger = LoggerFactory.getLogger(WebhookManager.class);

    private static final WebhookFsmMessages.Start START_MSG = new WebhookFsmMessages.Start();

    private final Hektor hektor;

    public static WebhookManager of(final Hektor hektor) {
        assertNotNull(hektor);
        return new WebhookManager(hektor);
    }

    private WebhookManager(final Hektor hektor) {
        this.hektor = hektor;
    }

    public Either<ErrorResult, Webhook> processWebhookRequest(final WebhookRequest request) {
        try {
            final var webhook = new Webhook(request);
            final var props = configureWebhookFsm(webhook);
            hektor.actorOf(webhook.sri(), props);
            return Either.right(webhook);
        } catch (final Throwable t) {
            final var cause = t.getCause() != null ? t.getCause() : t;
            final var errorDescription = Optional.ofNullable(cause.getMessage());
            final var error = new ErrorResult(500, cause.getClass().getSimpleName(), errorDescription);
            return Either.left(error);
        }
    }

    private static Props configureWebhookFsm(final Webhook webhook) {

        final OnStartFunction<WebhookFsmContext, WebhookData> onStart = (actorCtx, ctx, data) -> {
            actorCtx.self().tell(START_MSG);
        };

        return FsmActor.of(WebhookFsm.definition)
                .withContext(ref -> new WebhookFsmContext())
                .withData(() -> WebhookData.of(webhook))
                .withStartFunction(onStart)
                .build();

    }
}
