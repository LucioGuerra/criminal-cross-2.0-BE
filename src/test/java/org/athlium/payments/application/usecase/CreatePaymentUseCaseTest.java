package org.athlium.payments.application.usecase;

import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatePaymentUseCaseTest {

    private CreatePaymentUseCase useCase;
    private InMemoryPaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        useCase = new CreatePaymentUseCase();
        paymentRepository = new InMemoryPaymentRepository();
        useCase.paymentRepository = paymentRepository;
    }

    @Test
    void shouldCreatePaymentWithTodayDate() {
        Payment created = useCase.execute(new BigDecimal("10000"), "cash", 9L, 3L, 1L);

        assertEquals(1L, created.getId());
        assertEquals(new BigDecimal("10000"), created.getAmount());
        assertEquals("CASH", created.getMethod().name());
        assertEquals(LocalDate.now(), created.getPaidAt());
        assertEquals(9L, created.getClientId());
        assertEquals(3L, created.getHeadquartersId());
        assertEquals(1L, created.getOrganizationId());
    }

    @Test
    void shouldRejectWhenAmountIsInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> useCase.execute(BigDecimal.ZERO, "CARD", 9L, 3L, 1L));
        assertEquals("amount must be greater than 0", ex.getMessage());
    }

    @Test
    void shouldRejectWhenClientIdIsMissing() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> useCase.execute(new BigDecimal("10"), "CARD", null, 3L, 1L));
        assertEquals("clientId is required", ex.getMessage());
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        private final List<Payment> payments = new ArrayList<>();

        @Override
        public Payment save(Payment payment) {
            payment.setId((long) (payments.size() + 1));
            payments.add(payment);
            return payment;
        }

        @Override
        public Payment update(Payment payment) {
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            return payments.stream().filter(payment -> paymentId.equals(payment.getId())).findFirst();
        }

        @Override
        public boolean deleteById(Long paymentId) {
            return false;
        }

        @Override
        public PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria) {
            return new PageResponse<>(List.of(), criteria.page(), criteria.size(), 0);
        }
    }
}
