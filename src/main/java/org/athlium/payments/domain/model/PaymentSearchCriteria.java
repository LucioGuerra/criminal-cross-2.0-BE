package org.athlium.payments.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentSearchCriteria(
        String player,
        LocalDate paidAtFrom,
        LocalDate paidAtTo,
        Long clientId,
        PaymentMethod paymentMethod,
        BigDecimal amountMin,
        BigDecimal amountMax,
        Long headquartersId,
        Long organizationId,
        int page,
        int size,
        String sortColumn,
        boolean sortAscending) {
}
