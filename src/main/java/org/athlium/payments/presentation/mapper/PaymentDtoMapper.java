package org.athlium.payments.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.presentation.dto.ActivityResponse;
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
        response.setActivities(toActivityResponseList(payment.getActivities()));
        response.setClientId(payment.getClientId());
        response.setHeadquartersId(payment.getHeadquartersId());
        response.setOrganizationId(payment.getOrganizationId());
        return response;
    }

    private List<ActivityResponse> toActivityResponseList(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }

        return activities.stream().map(activity -> {
            ActivityResponse response = new ActivityResponse();
            response.setId(activity.getId());
            response.setName(activity.getName());
            response.setDescription(activity.getDescription());
            response.setIsActive(activity.getIsActive());
            response.setHqId(activity.getHqId());
            return response;
        }).toList();
    }
}
