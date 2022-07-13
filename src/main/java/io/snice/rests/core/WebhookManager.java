package io.snice.rests.core;

import io.snice.functional.Either;
import io.snice.rests.api.ErrorResult;
import io.snice.rests.api.Webhook;
import io.snice.rests.api.WebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class WebhookManager {

    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(WebhookManager.class);

    public static WebhookManager of(ScheduledExecutorService scheduler, HttpClient httpClient) {
        assertNotNull(scheduler);
        assertNotNull(httpClient);
        return new WebhookManager(scheduler, httpClient);
    }

    private WebhookManager(final ScheduledExecutorService executorService, final HttpClient httpClient) {
        this.scheduler = executorService;
        this.httpClient = httpClient;
    }

    public Either<ErrorResult, Webhook> processWebhookRequest(final WebhookRequest request) {
        try {
            final var webhook = new Webhook(request);
            final var job = new WebhookJob(1, webhook, httpClient, scheduler);
            webhook.request().initialDelay()
                    .ifPresentOrElse(delay -> scheduler.schedule(job, delay.getSeconds(), TimeUnit.SECONDS),
                            () -> scheduler.execute(job));

            return Either.right(webhook);
        } catch (final Throwable t) {
            final var cause = t.getCause() != null ? t.getCause() : t;
            final var errorDescription = Optional.ofNullable(cause.getMessage());
            final var error = new ErrorResult(500, cause.getClass().getSimpleName(), errorDescription);
            return Either.left(error);
        }
    }

    private static record WebhookJob(int currentExecution, Webhook webhook, HttpClient client, ScheduledExecutorService scheduler) implements Runnable{

        @Override
        public void run() {
            if (currentExecution < webhook.request().count()) {
                final var job = new WebhookJob(currentExecution + 1, webhook, client, scheduler);
                scheduler.schedule(job, webhook.request().subsequentDelay().getSeconds(), TimeUnit.SECONDS);
            }

            try {
                final var req = buildRequest();
                final var future = client.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray());
                future.thenAccept(WebhookJob::processResponse);
                future.exceptionally(WebhookJob::processError);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        private static HttpResponse<byte[]> processError(Throwable t) {
            // Currently, we don't care about the error, although we may care about
            // unresolvable addresses, connection timeouts and whatnot...
            return null;
        }

        private static void processResponse(HttpResponse<byte[]> response) {
            // Currently, don't care about the response
        }

        private HttpRequest buildRequest() {
            final var builder = HttpRequest.newBuilder(webhook.request().uri())
                    .method(webhook.request().method(), HttpRequest.BodyPublishers.noBody());

            return builder.build();
        }

    }
}
