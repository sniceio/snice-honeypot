package io.snice.rests.fsm;

import io.hektor.fsm.Data;
import io.snice.preconditions.PreConditions;
import io.snice.rests.api.Webhook;

import java.time.Duration;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class WebhookData implements Data {

    private final Webhook webhook;

    /**
     * How many webhooks we have issued so far.
     */
    private int count;

    public static WebhookData of(Webhook webhook) {
        assertNotNull(webhook);
        return new WebhookData(webhook);
    }

    private WebhookData(final Webhook webhook) {
        this.webhook = webhook;
    }

    public Optional<Duration> initialDelay() {
        return Optional.ofNullable(webhook.request().initialDelay());
    }

    /**
     * Everytime we issue a new webhook we need to count it.
     */
    public void incWebhooksIssued() {
        ++count;
    }

    /**
     * Check if there are more webhooks that should be issued.
     * @return
     */
    public boolean moreWebhooksToIssue() {
        return count < webhook.request().count();
    }
}
