package org.athlium.payments.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentListItem {

    private final Long id;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final LocalDate paidAt;
    private final String userName;
    private final String userLastName;
    private final Long clientId;
    private final Long headquartersId;
    private final Long organizationId;

    public PaymentListItem(Long id, BigDecimal amount, PaymentMethod paymentMethod, LocalDate paidAt,
            String userName, String userLastName, Long clientId, Long headquartersId, Long organizationId) {
        this.id = id;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.userName = userName;
        this.userLastName = userLastName;
        this.clientId = clientId;
        this.headquartersId = headquartersId;
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getHeadquartersId() {
        return headquartersId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}
