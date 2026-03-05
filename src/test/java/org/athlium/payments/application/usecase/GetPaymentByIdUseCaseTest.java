package org.athlium.payments.application.usecase;

import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetPaymentByIdUseCaseTest {

    private GetPaymentByIdUseCase useCase;
    private InMemoryPaymentRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new GetPaymentByIdUseCase();
        repository = new InMemoryPaymentRepository();
        useCase.paymentRepository = repository;
    }

    @Test
    void shouldThrowWhenIdIsInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> useCase.execute(0L));
        assertEquals("paymentId must be a positive number", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> useCase.execute(33L));
    }

    @Test
    void shouldReturnPaymentWhenExists() {
        Payment expected = new Payment();
        expected.setId(2L);
        repository.payment = expected;

        Payment result = useCase.execute(2L);

        assertSame(expected, result);
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        Payment payment;

        @Override
        public Payment save(Payment payment) {
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
        public PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria) {
            return new PageResponse<>(List.of(), criteria.page(), criteria.size(), 0);
        }
    }
}
