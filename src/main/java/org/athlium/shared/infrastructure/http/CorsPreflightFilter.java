package org.athlium.shared.infrastructure.http;

import jakarta.annotation.Priority;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class CorsPreflightFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        requestContext.abortWith(Response.noContent().build());
    }
}
