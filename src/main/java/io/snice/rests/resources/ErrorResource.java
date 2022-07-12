package io.snice.rests.resources;

import io.snice.rests.api.ErrorResult;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * No matter what you post to this resource, you'll get an error back.
 */
@Path("/error")
@Produces(MediaType.APPLICATION_JSON)
public class ErrorResource {

    @GET
    public Response getError(@QueryParam("code") Optional<Integer> errorCode, @QueryParam("message") Optional<String> message) {
        return generateError(errorCode, message);

    }

    @POST
    public Response postError(@QueryParam("code") Optional<Integer> errorCode, @QueryParam("message") Optional<String> message) {
        return generateError(errorCode, message);
    }

    private static Response generateError(Optional<Integer> errorCode, Optional<String> message) {
        final var error = new ErrorResult(errorCode.orElse(500), message.orElse("Server Error"));
        return Response.status(error.code(), error.message())
                .entity(error)
                .build();
    }
}
