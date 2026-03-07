package org.athlium.shared.infrastructure.http;

import jakarta.annotation.Priority;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class CorsPreflightFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ALLOW_METHODS = "GET,PUT,POST,PATCH,DELETE,OPTIONS";
    private static final String DEFAULT_ALLOW_HEADERS = "accept,authorization,content-type,x-requested-with,origin";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        Response.ResponseBuilder responseBuilder = Response.noContent();
        applyCorsHeaders(responseBuilder, requestContext);
        requestContext.abortWith(responseBuilder.build());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        applyCorsHeaders(responseContext.getHeaders(), requestContext);
    }

    private void applyCorsHeaders(Response.ResponseBuilder responseBuilder, ContainerRequestContext requestContext) {
        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");

        responseBuilder.header("Access-Control-Allow-Origin", "*");
        responseBuilder.header("Access-Control-Allow-Methods", ALLOW_METHODS);
        responseBuilder.header(
                "Access-Control-Allow-Headers",
                requestedHeaders == null || requestedHeaders.isBlank() ? DEFAULT_ALLOW_HEADERS : requestedHeaders
        );
        responseBuilder.header("Access-Control-Allow-Credentials", "false");
        responseBuilder.header("Access-Control-Max-Age", "86400");
    }

    private void applyCorsHeaders(MultivaluedMap<String, Object> headers, ContainerRequestContext requestContext) {
        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");

        headers.putSingle("Access-Control-Allow-Origin", "*");
        headers.putSingle("Access-Control-Allow-Methods", ALLOW_METHODS);
        headers.putSingle(
                "Access-Control-Allow-Headers",
                requestedHeaders == null || requestedHeaders.isBlank() ? DEFAULT_ALLOW_HEADERS : requestedHeaders
        );
        headers.putSingle("Access-Control-Allow-Credentials", "false");
        headers.putSingle("Access-Control-Max-Age", "86400");
    }
}
