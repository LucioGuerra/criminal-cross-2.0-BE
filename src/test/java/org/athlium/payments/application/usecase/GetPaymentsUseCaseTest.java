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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetPaymentsUseCaseTest {

    private GetPaymentsUseCase useCase;
    private InMemoryPaymentRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetPaymentsUseCase();
        repository = new InMemoryPaymentRepository();
        useCase.paymentRepository = repository;
    }

    @Test
    void shouldThrowWhenPageIsLessThanOne() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, null, null, null, null, null,
                        null, null, 0, 20, "paidAt:desc"));
        assertEquals("Page must be >= 1", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAmountRangeIsInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> useCase.execute(null, null, null, null, null,
                        new BigDecimal("100.00"), new BigDecimal("50.00"),
                        null, null, 1, 20, "paidAt:desc"));
        assertEquals("amountMin must be less than or equal to amountMax", exception.getMessage());
    }

    @Test
    void shouldDelegateWithNormalizedFiltersAndZeroBasedPage() {
        PageResponse<PaymentListItem> expected = new PageResponse<>(List.of(), 0, 20, 0);
        repository.findPaymentsResult = expected;

        PageResponse<PaymentListItem> result = useCase.execute(
                "  ana ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                5L,
                "cash",
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                3L,
                2L,
                1,
                20,
                "amount:asc"
        );

        assertSame(expected, result);
        assertEquals("ana", repository.lastCriteria.player());
        assertEquals("CASH", repository.lastCriteria.paymentMethod().name());
        assertEquals(0, repository.lastCriteria.page());
        assertEquals(20, repository.lastCriteria.size());
        assertEquals("amount", repository.lastCriteria.sortColumn());
        assertTrue(repository.lastCriteria.sortAscending());
        assertEquals(5L, repository.lastCriteria.clientId());
        assertEquals(3L, repository.lastCriteria.headquartersId());
        assertEquals(2L, repository.lastCriteria.organizationId());
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        PaymentSearchCriteria lastCriteria;
        PageResponse<PaymentListItem> findPaymentsResult = new PageResponse<>(List.of(), 0, 20, 0);

        @Override
        public Payment save(Payment payment) {
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            return Optional.empty();
        }

        @Override
        public PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria) {
            this.lastCriteria = criteria;
            return findPaymentsResult;
        }
    }
}
