package io.snice.rests.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;

import java.util.UUID;

/**
 * @param request the original {@link WebhookRequest} that created this {@link Webhook}
 */
public record Webhook(String sri, WebhookRequest request) {

    public Webhook(WebhookRequest request) {
        this("HOK" + Buffers.uuid().toHexString(false), request);
    }
}
