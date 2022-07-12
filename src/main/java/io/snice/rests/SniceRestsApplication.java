package io.snice.rests;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.hektor.core.Hektor;
import io.snice.rests.core.WebhookManager;
import io.snice.rests.health.DummyHealthCheck;
import io.snice.rests.resources.ErrorResource;
import io.snice.rests.resources.HappyResource;
import io.snice.rests.resources.WebhookResource;

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
        final var hektor = Hektor.withName("Rests").withConfiguration(config.getHektorConfig()).build();
        final var webhookManager = WebhookManager.of(hektor);

        environment.jersey().register(WebhookResource.of(webhookManager));
        environment.jersey().register(new HappyResource());
        environment.jersey().register(new ErrorResource());

        environment.healthChecks().register("dummy", new DummyHealthCheck());
    }

}
