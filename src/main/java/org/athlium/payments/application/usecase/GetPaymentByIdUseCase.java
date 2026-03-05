package org.athlium.payments.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class GetPaymentByIdUseCase {

    @Inject
    PaymentRepository paymentRepository;

    public Payment execute(Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            throw new BadRequestException("paymentId must be a positive number");
        }

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));
    }
}
