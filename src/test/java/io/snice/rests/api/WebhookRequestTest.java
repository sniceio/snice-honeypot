package io.snice.rests.api;

import net.bytebuddy.pool.TypePool;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebhookRequestTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "nisse", "g"})
    public void testCreate(String method) throws Exception {
        assertThat(create(method).method(), is("POST"));
    }

    @Test
    public void testCreateWhenNull() throws Exception {
        assertThat(create(null).method(), is("POST"));
    }

    @Test
    public void testUrlNull() {
        assertThrows(IllegalArgumentException.class, () -> new WebhookRequest(null, "POST"));
    }

    private static WebhookRequest create(String method) throws Exception {
        return new WebhookRequest(new URL("http://apa.com"), null);
    }

}