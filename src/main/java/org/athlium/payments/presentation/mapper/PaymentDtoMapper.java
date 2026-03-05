package org.athlium.payments.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.presentation.dto.PaymentListItemResponse;
import org.athlium.payments.presentation.dto.PaymentResponse;

import java.util.List;

@ApplicationScoped
public class PaymentDtoMapper {

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getMethod() != null ? payment.getMethod().name() : null);
        response.setPaidAt(payment.getPaidAt() != null ? payment.getPaidAt().toString() : null);
        response.setClientId(payment.getClientId());
        response.setHeadquartersId(payment.getHeadquartersId());
        response.setOrganizationId(payment.getOrganizationId());
        return response;
    }

    public List<PaymentListItemResponse> toListItemResponseList(List<PaymentListItem> payments) {
        return payments.stream().map(this::toListItemResponse).toList();
    }

    public PaymentListItemResponse toListItemResponse(PaymentListItem payment) {
        PaymentListItemResponse response = new PaymentListItemResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        response.setPaidAt(payment.getPaidAt());
        response.setUserName(payment.getUserName());
        response.setUserLastName(payment.getUserLastName());
        response.setClientId(payment.getClientId());
        response.setHeadquartersId(payment.getHeadquartersId());
        response.setOrganizationId(payment.getOrganizationId());
        return response;
    }
}
