package org.athlium.payments.presentation.dto;

import java.math.BigDecimal;

public class UpdatePaymentRequest {

    private BigDecimal amount;
    private String paymentMethod;
    private String paidAt;
    private Long clientId;
    private Long headquartersId;
    private Long organizationId;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getHeadquartersId() {
        return headquartersId;
    }

    public void setHeadquartersId(Long headquartersId) {
        this.headquartersId = headquartersId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
