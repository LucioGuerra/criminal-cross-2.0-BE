package org.athlium.payments.application.usecase;

import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePaymentUseCaseTest {

    private UpdatePaymentUseCase useCase;
    private InMemoryPaymentRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePaymentUseCase();
        repository = new InMemoryPaymentRepository();
        useCase.paymentRepository = repository;
    }

    @Test
    void shouldUpdatePaymentAndKeepPaidAtWhenNotProvided() {
        Payment existing = new Payment();
        existing.setId(7L);
        existing.setPaidAt(LocalDate.of(2026, 1, 10));
        repository.payment = existing;

        Payment updated = useCase.execute(
                7L,
                new BigDecimal("120.00"),
                "card",
                null,
                9L,
                3L,
                1L
        );

        assertNotNull(updated);
        assertEquals(7L, updated.getId());
        assertEquals(new BigDecimal("120.00"), updated.getAmount());
        assertEquals(PaymentMethod.CARD, updated.getMethod());
        assertEquals(LocalDate.of(2026, 1, 10), updated.getPaidAt());
        assertEquals(9L, updated.getClientId());
        assertEquals(3L, updated.getHeadquartersId());
        assertEquals(1L, updated.getOrganizationId());
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> useCase.execute(
                99L,
                new BigDecimal("50.00"),
                "cash",
                null,
                1L,
                1L,
                1L
        ));
    }

    @Test
    void shouldThrowWhenAmountIsInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> useCase.execute(
                2L,
                BigDecimal.ZERO,
                "cash",
                null,
                1L,
                1L,
                1L
        ));
        assertEquals("amount must be greater than 0", exception.getMessage());
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        Payment payment;

        @Override
        public Payment save(Payment payment) {
            return payment;
        }

        @Override
        public Payment update(Payment payment) {
            this.payment = payment;
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            if (payment != null && paymentId.equals(payment.getId())) {
                return Optional.of(payment);
            }
            return Optional.empty();
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
