package io.snice.rests.resources;

import io.snice.preconditions.PreConditions;
import io.snice.rests.api.Happy;
import io.snice.rests.api.WebhookRequest;
import io.snice.rests.core.WebhookManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * Resource for installing a webhook, which later will be invoked.
 */
@Path("/webhook")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class WebhookResource {

    private final WebhookManager manager;

    public static WebhookResource of(final WebhookManager manager) {
        PreConditions.assertNotNull(manager);
        return new WebhookResource(manager);
    }

    private WebhookResource(final WebhookManager manager) {
        this.manager = manager;
    }

    @POST
    public Response installWebhook(@FormParam("uri") @NotNull URI uri,
                                   @FormParam("method") @DefaultValue("POST") String method,
                                   @FormParam("count") @DefaultValue("1") int count,
                                   @FormParam("initialDelay") Optional<Integer> initialDelay,
                                   @FormParam("subsequentDelay") @DefaultValue("1") int subsequentDelay
                                   ) {
        final var initialDelayMaybe = initialDelay.map(Duration::ofSeconds);
        final var webhookRequest = new WebhookRequest(uri, method, count, initialDelayMaybe, Duration.ofSeconds(subsequentDelay));
        final var processed = manager.processWebhookRequest(webhookRequest);
        return processed.fold(error -> Response.status(error.code()).entity(error).build(),
                webhook -> Response.ok().entity(webhook).build());
    }

}
