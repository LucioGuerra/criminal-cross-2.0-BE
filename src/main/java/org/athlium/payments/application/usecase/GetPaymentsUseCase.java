package org.athlium.payments.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;

@ApplicationScoped
public class GetPaymentsUseCase {

    @Inject
    PaymentRepository paymentRepository;

    public PageResponse<PaymentListItem> execute(
            String player,
            LocalDate paidAtFrom,
            LocalDate paidAtTo,
            Long clientId,
            String paymentMethod,
            BigDecimal amountMin,
            BigDecimal amountMax,
            Long headquartersId,
            Long organizationId,
            int page,
            int size,
            String sort) {
        if (page < 1) {
            throw new BadRequestException("Page must be >= 1");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }
        if (paidAtFrom != null && paidAtTo != null && paidAtFrom.isAfter(paidAtTo)) {
            throw new BadRequestException("paidAtFrom must be less than or equal to paidAtTo");
        }
        if (amountMin != null && amountMax != null && amountMin.compareTo(amountMax) > 0) {
            throw new BadRequestException("amountMin must be less than or equal to amountMax");
        }

        validateOptionalPositiveId(clientId, "clientId");
        validateOptionalPositiveId(headquartersId, "headquartersId");
        validateOptionalPositiveId(organizationId, "organizationId");

        PaymentMethod parsedMethod = parseMethod(paymentMethod);
        SortSpec sortSpec = parseSort(sort);

        PaymentSearchCriteria criteria = new PaymentSearchCriteria(
                blankToNull(player),
                paidAtFrom,
                paidAtTo,
                clientId,
                parsedMethod,
                amountMin,
                amountMax,
                headquartersId,
                organizationId,
                page - 1,
                size,
                sortSpec.column(),
                sortSpec.ascending());

        return paymentRepository.findPayments(criteria);
    }

    private PaymentMethod parseMethod(String method) {
        if (method == null || method.isBlank()) {
            return null;
        }
        try {
            return PaymentMethod.valueOf(method.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("paymentMethod must be one of: CASH, CARD, TRANSFER, OTHER");
        }
    }

    private SortSpec parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new SortSpec("paid_at", false);
        }

        String[] parts = sort.trim().toLowerCase().split(":");
        if (parts.length != 2) {
            throw new BadRequestException("sort must follow <field>:<asc|desc>");
        }

        String column = switch (parts[0]) {
            case "paidat" -> "paid_at";
            case "amount" -> "amount";
            case "createdat" -> "created_at";
            case "id" -> "id";
            default -> throw new BadRequestException("sort field must be one of: paidAt, amount, createdAt, id");
        };

        boolean ascending = switch (parts[1]) {
            case "asc" -> true;
            case "desc" -> false;
            default -> throw new BadRequestException("sort direction must be asc or desc");
        };

        return new SortSpec(column, ascending);
    }

    private void validateOptionalPositiveId(Long value, String fieldName) {
        if (value != null && value <= 0) {
            throw new BadRequestException(fieldName + " must be a positive number");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record SortSpec(String column, boolean ascending) {
    }
}
