package org.athlium.payments.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.athlium.gym.presentation.dto.ActivityResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PaymentListItemResponse {

    private Long id;
    private BigDecimal amount;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paidAt;

    private String userName;
    private String userLastName;
    private List<ActivityResponse> activities;
    private PaymentPackageResponse paidPackage;
    private Long clientId;
    private Long headquartersId;
    private Long organizationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDate paidAt) {
        this.paidAt = paidAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public List<ActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
    }

    public PaymentPackageResponse getPaidPackage() {
        return paidPackage;
    }

    public void setPaidPackage(PaymentPackageResponse paidPackage) {
        this.paidPackage = paidPackage;
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
