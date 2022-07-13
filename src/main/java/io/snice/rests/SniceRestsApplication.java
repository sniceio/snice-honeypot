package io.snice.rests;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.snice.rests.core.WebhookManager;
import io.snice.rests.health.DummyHealthCheck;
import io.snice.rests.resources.ErrorResource;
import io.snice.rests.resources.HappyResource;
import io.snice.rests.resources.WebhookResource;
import io.snice.util.concurrent.SniceThreadFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Super simple REST Service (rests) for testing other systems.
 */
public class SniceRestsApplication extends Application<SniceRestsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new SniceRestsApplication().run(args);
    }

    @Override
    public String getName() {
        return "SniceRests";
    }

    @Override
    public void initialize(final Bootstrap<SniceRestsConfiguration> bootstrap) {
    }

    @Override
    public void run(final SniceRestsConfiguration config,
                    final Environment environment) {
        final int maxHttpThreads = 10;
        final var httpThreadPool = Executors.newFixedThreadPool(maxHttpThreads, SniceThreadFactory.withNamePrefix("HTTP").build());
        final var httpClient = HttpClient.newBuilder().executor(httpThreadPool)
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(25))
                .build();

        final var scheduler = Executors.newScheduledThreadPool(1, SniceThreadFactory.withNamePrefix("Timer").build());
        final var webhookManager = WebhookManager.of(scheduler, httpClient);

        environment.jersey().register(WebhookResource.of(webhookManager));
        environment.jersey().register(new HappyResource());
        environment.jersey().register(new ErrorResource());

        environment.healthChecks().register("dummy", new DummyHealthCheck());
    }

}
