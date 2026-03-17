package org.athlium.shared.infrastructure.http;

import jakarta.annotation.Priority;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsResponseFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "cors.allowed-origins", defaultValue = "*")
    String allowedOriginsConfig;

    @ConfigProperty(name = "cors.allowed-methods", defaultValue = "GET,POST,PUT,PATCH,DELETE,OPTIONS")
    String allowedMethods;

    @ConfigProperty(name = "cors.allowed-headers", defaultValue = "authorization,content-type,x-idempotency-key")
    String allowedHeaders;

    @ConfigProperty(name = "cors.max-age-seconds", defaultValue = "3600")
    String maxAgeSeconds;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || origin.isBlank()) {
            return;
        }

        if (!isAllowedOrigin(origin)) {
            return;
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", resolveAllowOriginHeader(origin));
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", allowedMethods);

        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
        responseContext.getHeaders().putSingle(
                "Access-Control-Allow-Headers",
                requestedHeaders != null && !requestedHeaders.isBlank() ? requestedHeaders : allowedHeaders
        );
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", maxAgeSeconds);

        if (!hasWildcardOrigin()) {
            responseContext.getHeaders().putSingle("Vary", "Origin");
        }

        if (HttpMethod.OPTIONS.equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.getHeaders().putSingle("Content-Length", "0");
        }
    }

    private boolean isAllowedOrigin(String origin) {
        if (hasWildcardOrigin()) {
            return true;
        }

        return parseAllowedOrigins().contains(origin);
    }

    private String resolveAllowOriginHeader(String requestOrigin) {
        if (hasWildcardOrigin()) {
            return "*";
        }
        return requestOrigin;
    }

    private boolean hasWildcardOrigin() {
        return parseAllowedOrigins().stream().anyMatch("*"::equals);
    }

    private List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
