package org.athlium.payments.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.presentation.dto.PaymentResponse;

@ApplicationScoped
public class PaymentDtoMapper {

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getMethod() != null ? payment.getMethod().name() : null);
        response.setPaidAt(payment.getPaidAt() != null ? payment.getPaidAt().toString() : null);
        return response;
    }
}
