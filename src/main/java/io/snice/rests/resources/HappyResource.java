package io.snice.rests.resources;

import io.snice.rests.api.Happy;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * No matter what you get/post to this resource, you'll get a 200 OK back
 */
@Path("/happy")
@Produces(MediaType.APPLICATION_JSON)
public class HappyResource {

    @GET
    public Response getHappy(@QueryParam("message") Optional<String> message) {
        return generateHappy(message);

    }

    @POST
    public Response postError(@QueryParam("message") Optional<String> message) {
        return generateHappy(message);
    }

    private static Response generateHappy(Optional<String> message) {
        final var happy = new Happy(message.orElseGet(HappyResource::randomHappyMessage));
        return Response.ok()
                .entity(happy)
                .build();
    }

    private static String randomHappyMessage() {
        return "What a wonderful day";
    }
}
