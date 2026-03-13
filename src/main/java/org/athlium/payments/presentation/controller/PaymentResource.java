package org.athlium.payments.presentation.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.athlium.auth.infrastructure.security.Authenticated;
import org.athlium.payments.application.usecase.CreatePaymentUseCase;
import org.athlium.payments.application.usecase.DeletePaymentUseCase;
import org.athlium.payments.application.usecase.GetPaymentByIdUseCase;
import org.athlium.payments.application.usecase.GetPaymentsUseCase;
import org.athlium.payments.application.usecase.UpdatePaymentUseCase;
import org.athlium.payments.presentation.dto.CreatePaymentRequest;
import org.athlium.payments.presentation.dto.PaymentListItemResponse;
import org.athlium.payments.presentation.dto.UpdatePaymentRequest;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.payments.presentation.mapper.PaymentDtoMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated(roles = {"SUPERADMIN", "ORG_ADMIN", "PROFESSOR"})
public class PaymentResource {

    @Inject
    CreatePaymentUseCase createPaymentUseCase;

    @Inject
    GetPaymentsUseCase getPaymentsUseCase;

    @Inject
    GetPaymentByIdUseCase getPaymentByIdUseCase;

    @Inject
    UpdatePaymentUseCase updatePaymentUseCase;

    @Inject
    DeletePaymentUseCase deletePaymentUseCase;

    @Inject
    PaymentDtoMapper paymentDtoMapper;

    @POST
    public Response createPayment(CreatePaymentRequest request) {
        try {
            var payment = createPaymentUseCase.execute(
                    request != null ? request.getAmount() : null,
                    request != null ? request.getPaymentMethod() : null,
                    request != null ? request.getClientId() : null,
                    request != null ? request.getHeadquartersId() : null,
                    request != null ? request.getOrganizationId() : null
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

    @GET
    public Response getPayments(
            @QueryParam("player") String player,
            @QueryParam("paidAtFrom") String paidAtFrom,
            @QueryParam("paidAtTo") String paidAtTo,
            @QueryParam("clientId") Long clientId,
            @QueryParam("paymentMethod") String paymentMethod,
            @QueryParam("amountMin") BigDecimal amountMin,
            @QueryParam("amountMax") BigDecimal amountMax,
            @QueryParam("headquartersId") Long headquartersId,
            @QueryParam("organizationId") Long organizationId,
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("20") @QueryParam("size") int size,
            @DefaultValue("paidAt:desc") @QueryParam("sort") String sort) {
        try {
            var paymentsPage = getPaymentsUseCase.execute(
                    player,
                    parseLocalDate(paidAtFrom, "paidAtFrom"),
                    parseLocalDate(paidAtTo, "paidAtTo"),
                    clientId,
                    paymentMethod,
                    amountMin,
                    amountMax,
                    headquartersId,
                    organizationId,
                    page,
                    size,
                    sort
            );
            List<PaymentListItemResponse> content = paymentDtoMapper.toListItemResponseList(paymentsPage.getContent());
            PageResponse<PaymentListItemResponse> mappedPage = new PageResponse<>(
                    content,
                    paymentsPage.getPage() + 1,
                    paymentsPage.getSize(),
                    paymentsPage.getTotalElements());

            return Response.ok(ApiResponse.success("Payments retrieved", mappedPage)).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPaymentById(@PathParam("id") Long id) {
        try {
            var payment = getPaymentByIdUseCase.execute(id);
            return Response.ok(ApiResponse.success("Payment retrieved", paymentDtoMapper.toResponse(payment))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePayment(@PathParam("id") Long id, UpdatePaymentRequest request) {
        try {
            var updated = updatePaymentUseCase.execute(
                    id,
                    request != null ? request.getAmount() : null,
                    request != null ? request.getPaymentMethod() : null,
                    parseLocalDate(request != null ? request.getPaidAt() : null, "paidAt"),
                    request != null ? request.getClientId() : null,
                    request != null ? request.getHeadquartersId() : null,
                    request != null ? request.getOrganizationId() : null
            );
            return Response.ok(ApiResponse.success("Payment updated", paymentDtoMapper.toResponse(updated))).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletePayment(@PathParam("id") Long id) {
        try {
            deletePaymentUseCase.execute(id);
            return Response.ok(ApiResponse.success("Payment deleted", null)).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        }
    }

    private LocalDate parseLocalDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " must be a valid ISO-8601 date (yyyy-MM-dd)");
        }
    }
}
