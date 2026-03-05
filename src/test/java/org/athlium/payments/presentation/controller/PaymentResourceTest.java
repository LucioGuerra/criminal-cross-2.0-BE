package org.athlium.payments.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.DefaultValue;
import org.athlium.payments.application.usecase.CreatePaymentUseCase;
import org.athlium.payments.application.usecase.GetPaymentByIdUseCase;
import org.athlium.payments.application.usecase.GetPaymentsUseCase;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.presentation.dto.CreatePaymentRequest;
import org.athlium.payments.presentation.dto.PaymentListItemResponse;
import org.athlium.payments.presentation.mapper.PaymentDtoMapper;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.dto.ApiResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentResourceTest {

    @Test
    void shouldReturnPaginatedPaymentsWithDynamicFilters() {
        GetPaymentsUseCase getPaymentsUseCase = new GetPaymentsUseCase() {
            @Override
            public PageResponse<PaymentListItem> execute(String player, LocalDate paidAtFrom, LocalDate paidAtTo,
                    Long clientId, String paymentMethod, BigDecimal amountMin, BigDecimal amountMax,
                    Long headquartersId, Long organizationId, int page, int size, String sort) {
                return new PageResponse<>(
                        List.of(new PaymentListItem(7L, new BigDecimal("45.50"), PaymentMethod.CARD,
                                LocalDate.of(2026, 1, 10), "Ana", "Lopez", 55L, 3L, 1L)),
                        0,
                        20,
                        1
                );
            }
        };

        PaymentResource resource = new PaymentResource();
        resource.getPaymentsUseCase = getPaymentsUseCase;
        resource.getPaymentByIdUseCase = new GetPaymentByIdUseCase();
        resource.createPaymentUseCase = new CreatePaymentUseCase();
        resource.paymentDtoMapper = new PaymentDtoMapper();

        var response = resource.getPayments("ana", null, null, null, null, null, null,
                null, null, 1, 20, "paidAt:desc");

        assertEquals(200, response.getStatus());
        Object entity = response.getEntity();
        assertNotNull(entity);
        assertInstanceOf(ApiResponse.class, entity);

        ApiResponse<?> apiResponse = (ApiResponse<?>) entity;
        assertTrue(apiResponse.isSuccess());
        assertEquals("Payments retrieved", apiResponse.getMessage());

        @SuppressWarnings("unchecked")
        PageResponse<PaymentListItemResponse> data = (PageResponse<PaymentListItemResponse>) apiResponse.getData();
        assertEquals(1, data.getContent().size());
        assertEquals(55L, data.getContent().getFirst().getClientId());
        assertEquals(3L, data.getContent().getFirst().getHeadquartersId());
        assertEquals(1L, data.getContent().getFirst().getOrganizationId());
    }

    @Test
    void shouldReturnBadRequestWhenFiltersAreInvalid() {
        GetPaymentsUseCase getPaymentsUseCase = new GetPaymentsUseCase() {
            @Override
            public PageResponse<PaymentListItem> execute(String player, LocalDate paidAtFrom, LocalDate paidAtTo,
                    Long clientId, String paymentMethod, BigDecimal amountMin, BigDecimal amountMax,
                    Long headquartersId, Long organizationId, int page, int size, String sort) {
                throw new BadRequestException("amountMin must be less than or equal to amountMax");
            }
        };

        PaymentResource resource = new PaymentResource();
        resource.getPaymentsUseCase = getPaymentsUseCase;
        resource.getPaymentByIdUseCase = new GetPaymentByIdUseCase();
        resource.createPaymentUseCase = new CreatePaymentUseCase();
        resource.paymentDtoMapper = new PaymentDtoMapper();

        var response = resource.getPayments(null, null, null, null, null, null, null,
                null, null, 1, 20, "paidAt:desc");

        assertEquals(400, response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("amountMin must be less than or equal to amountMax", apiResponse.getMessage());
    }

    @Test
    void shouldReturnPaymentById() {
        GetPaymentByIdUseCase getPaymentByIdUseCase = new GetPaymentByIdUseCase() {
            @Override
            public Payment execute(Long paymentId) {
                Payment payment = new Payment();
                payment.setId(paymentId);
                payment.setAmount(new BigDecimal("100.00"));
                payment.setMethod(PaymentMethod.CASH);
                payment.setPaidAt(LocalDate.of(2026, 1, 15));
                payment.setClientId(77L);
                payment.setHeadquartersId(3L);
                payment.setOrganizationId(1L);
                return payment;
            }
        };

        PaymentResource resource = new PaymentResource();
        resource.getPaymentsUseCase = new GetPaymentsUseCase();
        resource.getPaymentByIdUseCase = getPaymentByIdUseCase;
        resource.createPaymentUseCase = new CreatePaymentUseCase();
        resource.paymentDtoMapper = new PaymentDtoMapper();

        var response = resource.getPaymentById(11L);

        assertEquals(200, response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Payment retrieved", apiResponse.getMessage());
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist() {
        GetPaymentByIdUseCase getPaymentByIdUseCase = new GetPaymentByIdUseCase() {
            @Override
            public Payment execute(Long paymentId) {
                throw new EntityNotFoundException("Payment", paymentId);
            }
        };

        PaymentResource resource = new PaymentResource();
        resource.getPaymentsUseCase = new GetPaymentsUseCase();
        resource.getPaymentByIdUseCase = getPaymentByIdUseCase;
        resource.createPaymentUseCase = new CreatePaymentUseCase();
        resource.paymentDtoMapper = new PaymentDtoMapper();

        var response = resource.getPaymentById(404L);

        assertEquals(404, response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void shouldDeclareDefaultPaginationAndSortQueryParamsForUnifiedListEndpoint() throws Exception {
        Method listMethod = PaymentResource.class.getMethod(
                "getPayments", String.class, String.class, String.class, Long.class, String.class,
                BigDecimal.class, BigDecimal.class, Long.class, Long.class, int.class, int.class, String.class);
        Parameter[] params = listMethod.getParameters();

        DefaultValue pageDefault = params[9].getAnnotation(DefaultValue.class);
        DefaultValue sizeDefault = params[10].getAnnotation(DefaultValue.class);
        DefaultValue sortDefault = params[11].getAnnotation(DefaultValue.class);
        assertNotNull(pageDefault);
        assertNotNull(sizeDefault);
        assertNotNull(sortDefault);
        assertEquals("1", pageDefault.value());
        assertEquals("20", sizeDefault.value());
        assertEquals("paidAt:desc", sortDefault.value());
    }

    @Test
    void shouldNotExposeDeprecatedScopedListEndpoints() {
        assertThrows(NoSuchMethodException.class,
                () -> PaymentResource.class.getMethod("getPaymentsByHeadquarters", Long.class, int.class, int.class));
        assertThrows(NoSuchMethodException.class,
                () -> PaymentResource.class.getMethod("getPaymentsByOrganization", Long.class, int.class, int.class));
    }

    @Test
    void shouldSerializePaidAtAsIsoDate() throws Exception {
        PaymentListItemResponse response = new PaymentListItemResponse();
        response.setId(1L);
        response.setPaidAt(LocalDate.of(2026, 1, 10));

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"paidAt\":\"2026-01-10\""));
    }

    @Test
    void shouldKeepCreatePaymentBehavior() {
        CreatePaymentUseCase createPaymentUseCase = new CreatePaymentUseCase() {
            @Override
            public Payment execute(BigDecimal amount, String method, Long clientId, Long headquartersId,
                    Long organizationId) {
                Payment payment = new Payment();
                payment.setId(11L);
                payment.setAmount(amount);
                payment.setMethod(PaymentMethod.valueOf(method));
                payment.setPaidAt(LocalDate.of(2026, 1, 15));
                payment.setClientId(clientId);
                payment.setHeadquartersId(headquartersId);
                payment.setOrganizationId(organizationId);
                return payment;
            }
        };

        PaymentResource resource = new PaymentResource();
        resource.createPaymentUseCase = createPaymentUseCase;
        resource.getPaymentsUseCase = new GetPaymentsUseCase();
        resource.getPaymentByIdUseCase = new GetPaymentByIdUseCase();
        resource.paymentDtoMapper = new PaymentDtoMapper();

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("CASH");
        request.setClientId(77L);
        request.setHeadquartersId(3L);
        request.setOrganizationId(1L);

        var response = resource.createPayment(request);

        assertEquals(201, response.getStatus());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Payment created", apiResponse.getMessage());
    }
}
