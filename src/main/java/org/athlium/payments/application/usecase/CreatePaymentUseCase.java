package org.athlium.payments.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@ApplicationScoped
public class CreatePaymentUseCase {

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    public Payment execute(BigDecimal amount, String method) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be greater than 0");
        }

        PaymentMethod paymentMethod = parseMethod(method);

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setMethod(paymentMethod);
        payment.setPaidAt(LocalDate.now(ZoneOffset.UTC));
        return paymentRepository.save(payment);
    }

    private PaymentMethod parseMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new BadRequestException("paymentMethod is required");
        }
        try {
            return PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("paymentMethod must be one of: CASH, CARD, TRANSFER, OTHER");
        }
    }
}
