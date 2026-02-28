package org.athlium.payments.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.payments.application.usecase.CreatePaymentUseCase;
import org.athlium.payments.presentation.dto.CreatePaymentRequest;
import org.athlium.payments.presentation.mapper.PaymentDtoMapper;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN", "PROFESSOR"})
public class PaymentResource {

    @Inject
    CreatePaymentUseCase createPaymentUseCase;

    @Inject
    PaymentDtoMapper paymentDtoMapper;

    @POST
    public Response createPayment(CreatePaymentRequest request) {
        try {
            var payment = createPaymentUseCase.execute(
                    request != null ? request.getAmount() : null,
                    request != null ? request.getPaymentMethod() : null
            );

            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Payment created", paymentDtoMapper.toResponse(payment)))
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }
}
