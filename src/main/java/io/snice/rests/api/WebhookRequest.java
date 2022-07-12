package io.snice.rests.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.preconditions.PreConditions;

import javax.ws.rs.FormParam;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * @param url the URL of the actual webhook to hit
 * @param method the HTTP method to use when hitting up the webhook
 * @param count how many times we are supposed to hit the webhook up.
 * @param initialDelay how long to wait before issuing the very first webhook request.
 * @param subsequentDelay how long we are to wait between webhook requests.
 *                        Only matters if the count is greater than one.
 */
public record WebhookRequest(URL url,
                             String method,
                             int count,
                             Duration initialDelay,
                             Duration subsequentDelay) {

    public WebhookRequest {
        assertNotNull(url, "The webhook URL cannot be null");
        method = processMethod(method);
    }

    public WebhookRequest(URL url, String method) {
        this(url, method, 1, Duration.ofSeconds(10), Duration.ofSeconds(1));
    }

    private static String processMethod(final String method) {
        final var m = method == null ? "POST" : method.toUpperCase();
        return switch (m) {
            case "POST", "GET", "DELETE", "PUT", "HEAD", "OPTIONS", "TRACE", "CONNECT" -> m;
            default -> "POST";
        };
    }
}
