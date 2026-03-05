package org.athlium.payments.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.payments.infrastructure.entity.PaymentEntity;
import org.athlium.shared.domain.PageResponse;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    @Inject
    PaymentPanacheRepository paymentPanacheRepository;

    @Inject
    EntityManager em;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.amount = payment.getAmount();
        entity.method = payment.getMethod();
        entity.paidAt = payment.getPaidAt();
        entity.clientId = payment.getClientId();
        entity.headquartersId = payment.getHeadquartersId();
        entity.organizationId = payment.getOrganizationId();
        paymentPanacheRepository.persist(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentPanacheRepository.findByIdOptional(paymentId).map(this::toDomain);
    }

    @Override
    public PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria) {
        Map<String, Object> params = new LinkedHashMap<>();
        String whereClause = buildWhereClause(criteria, params);

        Query countQuery = em.createNativeQuery(buildCountSql(whereClause));
        setQueryParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), criteria.page(), criteria.size(), 0);
        }

        Query dataQuery = em.createNativeQuery(buildDataSql(whereClause, criteria.sortColumn(), criteria.sortAscending()));
        setQueryParameters(dataQuery, params);
        dataQuery.setParameter("size", criteria.size());
        dataQuery.setParameter("offset", criteria.page() * criteria.size());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<PaymentListItem> items = rows.stream().map(this::mapRow).toList();

        return new PageResponse<>(items, criteria.page(), criteria.size(), total);
    }

    private Payment toDomain(PaymentEntity entity) {
        Payment payment = new Payment();
        payment.setId(entity.id);
        payment.setAmount(entity.amount);
        payment.setMethod(entity.method);
        payment.setPaidAt(entity.paidAt);
        payment.setClientId(entity.clientId);
        payment.setHeadquartersId(entity.headquartersId);
        payment.setOrganizationId(entity.organizationId);
        payment.setCreatedAt(entity.createdAt);
        payment.setUpdatedAt(entity.updatedAt);
        return payment;
    }

    private String buildWhereClause(PaymentSearchCriteria criteria, Map<String, Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");

        if (criteria.player() != null) {
            where.append(" AND (LOWER(u.name) LIKE :player OR LOWER(u.last_name) LIKE :player)");
            params.put("player", "%" + criteria.player().toLowerCase() + "%");
        }
        if (criteria.paidAtFrom() != null) {
            where.append(" AND p.paid_at >= :paidAtFrom");
            params.put("paidAtFrom", criteria.paidAtFrom());
        }
        if (criteria.paidAtTo() != null) {
            where.append(" AND p.paid_at <= :paidAtTo");
            params.put("paidAtTo", criteria.paidAtTo());
        }
        if (criteria.clientId() != null) {
            where.append(" AND p.client_id = :clientId");
            params.put("clientId", criteria.clientId());
        }
        if (criteria.paymentMethod() != null) {
            where.append(" AND p.method = :paymentMethod");
            params.put("paymentMethod", criteria.paymentMethod().name());
        }
        if (criteria.amountMin() != null) {
            where.append(" AND p.amount >= :amountMin");
            params.put("amountMin", criteria.amountMin());
        }
        if (criteria.amountMax() != null) {
            where.append(" AND p.amount <= :amountMax");
            params.put("amountMax", criteria.amountMax());
        }
        if (criteria.headquartersId() != null) {
            where.append(" AND p.headquarters_id = :headquartersId");
            params.put("headquartersId", criteria.headquartersId());
        }
        if (criteria.organizationId() != null) {
            where.append(" AND p.organization_id = :organizationId");
            params.put("organizationId", criteria.organizationId());
        }

        return where.toString();
    }

    private String buildCountSql(String whereClause) {
        return """
                SELECT COUNT(*)
                FROM payments p
                LEFT JOIN users u ON u.id = p.client_id
                %s
                """.formatted(whereClause);
    }

    private String buildDataSql(String whereClause, String sortColumn, boolean sortAscending) {
        String direction = sortAscending ? "ASC" : "DESC";
        return """
                SELECT p.id,
                       p.amount,
                       p.method,
                       p.paid_at,
                       u.name,
                       u.last_name,
                       p.client_id,
                       p.headquarters_id,
                       p.organization_id
                FROM payments p
                LEFT JOIN users u ON u.id = p.client_id
                %s
                ORDER BY p.%s %s, p.id DESC
                LIMIT :size OFFSET :offset
                """.formatted(whereClause, sortColumn, direction);
    }

    private void setQueryParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private PaymentListItem mapRow(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        BigDecimal amount = (BigDecimal) row[1];
        PaymentMethod paymentMethod = parsePaymentMethod(row[2]);
        LocalDate paidAt = parseLocalDate(row[3]);
        String userName = (String) row[4];
        String userLastName = (String) row[5];
        Long clientId = ((Number) row[6]).longValue();
        Long headquartersId = ((Number) row[7]).longValue();
        Long organizationId = ((Number) row[8]).longValue();
        return new PaymentListItem(id, amount, paymentMethod, paidAt, userName, userLastName, clientId,
                headquartersId, organizationId);
    }

    private PaymentMethod parsePaymentMethod(Object value) {
        if (value instanceof PaymentMethod paymentMethod) {
            return paymentMethod;
        }
        return PaymentMethod.valueOf(value.toString().trim().toUpperCase());
    }

    private LocalDate parseLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
