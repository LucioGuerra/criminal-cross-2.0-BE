package org.athlium.payments.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;

@ApplicationScoped
public class UpdatePaymentUseCase {

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    public Payment execute(Long paymentId, BigDecimal amount, String method, LocalDate paidAt,
            Long clientId, Long headquartersId, Long organizationId) {
        validateRequiredId(paymentId, "paymentId");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be greater than 0");
        }
        validateRequiredId(clientId, "clientId");
        validateRequiredId(headquartersId, "headquartersId");
        validateRequiredId(organizationId, "organizationId");

        Payment existing = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));

        Payment payment = new Payment();
        payment.setId(existing.getId());
        payment.setAmount(amount);
        payment.setMethod(parseMethod(method));
        payment.setPaidAt(paidAt != null ? paidAt : existing.getPaidAt());
        payment.setClientId(clientId);
        payment.setHeadquartersId(headquartersId);
        payment.setOrganizationId(organizationId);
        Payment updated = paymentRepository.update(payment);
        if (updated == null) {
            throw new EntityNotFoundException("Payment", paymentId);
        }
        return updated;
    }

    private void validateRequiredId(Long value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        if (value <= 0) {
            throw new BadRequestException(fieldName + " must be a positive number");
        }
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
