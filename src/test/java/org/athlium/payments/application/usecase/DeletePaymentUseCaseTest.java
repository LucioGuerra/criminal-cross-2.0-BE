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
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeletePaymentUseCaseTest {

    private DeletePaymentUseCase useCase;
    private InMemoryPaymentRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new DeletePaymentUseCase();
        repository = new InMemoryPaymentRepository();
        useCase.paymentRepository = repository;
    }

    @Test
    void shouldDeletePaymentById() {
        repository.deleteResult = true;

        useCase.execute(9L);

        assertEquals(9L, repository.deletedId);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> useCase.execute(77L));
    }

    @Test
    void shouldThrowWhenIdIsInvalid() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> useCase.execute(0L));
        assertEquals("paymentId must be a positive number", exception.getMessage());
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        Long deletedId;
        boolean deleteResult;

        @Override
        public Payment save(Payment payment) {
            return payment;
        }

        @Override
        public Payment update(Payment payment) {
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            return Optional.empty();
        }

        @Override
        public boolean deleteById(Long paymentId) {
            this.deletedId = paymentId;
            return deleteResult;
        }

        @Override
        public PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria) {
            return new PageResponse<>(List.of(), criteria.page(), criteria.size(), 0);
        }
    }
}
