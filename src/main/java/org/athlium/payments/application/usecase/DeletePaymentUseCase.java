package org.athlium.payments.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class DeletePaymentUseCase {

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    public void execute(Long paymentId) {
        if (paymentId == null) {
            throw new BadRequestException("paymentId is required");
        }
        if (paymentId <= 0) {
            throw new BadRequestException("paymentId must be a positive number");
        }

        if (!paymentRepository.deleteById(paymentId)) {
            throw new EntityNotFoundException("Payment", paymentId);
        }
    }
}
