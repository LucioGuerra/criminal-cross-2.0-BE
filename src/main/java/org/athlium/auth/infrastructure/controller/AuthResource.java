package org.athlium.auth.infrastructure.controller;

import org.athlium.auth.application.service.AuthService;
import org.athlium.shared.dto.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Controlador REST para endpoints de autenticación
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @GET
    @Path("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Auth module is running");
    }
}