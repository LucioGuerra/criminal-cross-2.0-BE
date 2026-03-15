package org.athlium.payments.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.athlium.gym.domain.model.Activity;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.model.PaymentPackageActivity;
import org.athlium.payments.domain.model.PaymentPackageInfo;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.payments.infrastructure.entity.PaymentEntity;
import org.athlium.shared.domain.PageResponse;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    private static final Logger LOG = Logger.getLogger(PaymentRepositoryImpl.class);

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
    public Payment update(Payment payment) {
        PaymentEntity entity = paymentPanacheRepository.findById(payment.getId());
        if (entity == null) {
            return null;
        }

        entity.amount = payment.getAmount();
        entity.method = payment.getMethod();
        entity.paidAt = payment.getPaidAt();
        entity.clientId = payment.getClientId();
        entity.headquartersId = payment.getHeadquartersId();
        entity.organizationId = payment.getOrganizationId();
        return toDomain(entity);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentPanacheRepository.findByIdOptional(paymentId).map(this::toDomain);
    }

    @Override
    public boolean deleteById(Long paymentId) {
        return paymentPanacheRepository.deleteById(paymentId);
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

        List<Long> paymentIds = items.stream().map(PaymentListItem::getId).toList();
        Map<Long, PaymentPackageInfo> paidPackageByPayment = findPaidPackageByPaymentIds(paymentIds);
        List<PaymentListItem> enrichedItems = items.stream()
                .map(item -> new PaymentListItem(
                        item.getId(),
                        item.getAmount(),
                        item.getPaymentMethod(),
                        item.getPaidAt(),
                        item.getUserName(),
                        item.getUserLastName(),
                        extractActivities(paidPackageByPayment.get(item.getId())),
                        paidPackageByPayment.get(item.getId()),
                        item.getClientId(),
                        item.getHeadquartersId(),
                        item.getOrganizationId()
                ))
                .toList();

        return new PageResponse<>(enrichedItems, criteria.page(), criteria.size(), total);
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
        BigDecimal amount = toBigDecimal(row[1]);
        PaymentMethod paymentMethod = parsePaymentMethod(row[2]);
        LocalDate paidAt = parseLocalDate(row[3]);
        String userName = (String) row[4];
        String userLastName = (String) row[5];
        Long clientId = toLong(row[6]);
        Long headquartersId = toLong(row[7]);
        Long organizationId = toLong(row[8]);
        return new PaymentListItem(id, amount, paymentMethod, paidAt, userName, userLastName, List.of(), null, clientId,
                headquartersId, organizationId);
    }

    private Map<Long, PaymentPackageInfo> findPaidPackageByPaymentIds(List<Long> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows;
        try {
            Query query = em.createNativeQuery("""
                    SELECT cp.payment_id,
                           cp.id,
                           a.id,
                           a.name,
                           a.description,
                           a.is_active,
                           a.hq_id,
                           cpc.tokens
                    FROM client_packages cp
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                    WHERE cp.payment_id IN (:paymentIds)
                    ORDER BY cp.payment_id, cp.id DESC, a.name
                    """);
            query.setParameter("paymentIds", paymentIds);

            @SuppressWarnings("unchecked")
            List<Object[]> resultRows = query.getResultList();
            rows = resultRows;
        } catch (RuntimeException ex) {
            LOG.warn("Failed to fetch package info for payments list. Returning payments without package details.", ex);
            return Map.of();
        }

        Map<Long, PackageAccumulator> accumulators = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long paymentId = ((Number) row[0]).longValue();
            Long packageId = toLong(row[1]);
            PackageAccumulator accumulator = accumulators.computeIfAbsent(paymentId,
                    key -> new PackageAccumulator(packageId));

            if (accumulator.packageId == null && packageId != null) {
                accumulator.packageId = packageId;
            }

            if (accumulator.packageId != null && packageId != null && !accumulator.packageId.equals(packageId)) {
                continue;
            }

            Long activityId = toLong(row[2]);
            if (activityId == null || accumulator.activityIds.contains(activityId)) {
                continue;
            }

            Activity activity = new Activity();
            activity.setId(activityId);
            activity.setName((String) row[3]);
            activity.setDescription((String) row[4]);
            activity.setIsActive(parseBoolean(row[5]));
            activity.setHqId(toLong(row[6]));

            PaymentPackageActivity packageActivity = new PaymentPackageActivity(activity, toInteger(row[7]));
            accumulator.activities.add(packageActivity);
            accumulator.activityIds.add(activityId);
        }

        Map<Long, PaymentPackageInfo> packagesByPayment = new LinkedHashMap<>();
        for (Map.Entry<Long, PackageAccumulator> entry : accumulators.entrySet()) {
            PackageAccumulator value = entry.getValue();
            packagesByPayment.put(entry.getKey(), new PaymentPackageInfo(value.packageId, value.activities));
        }

        return packagesByPayment;
    }

    private List<Activity> extractActivities(PaymentPackageInfo paidPackage) {
        if (paidPackage == null || paidPackage.getActivities() == null || paidPackage.getActivities().isEmpty()) {
            return List.of();
        }

        return paidPackage.getActivities().stream()
                .map(PaymentPackageActivity::getActivity)
                .toList();
    }

    private Boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private PaymentMethod parsePaymentMethod(Object value) {
        if (value == null) {
            return PaymentMethod.OTHER;
        }
        if (value instanceof PaymentMethod paymentMethod) {
            return paymentMethod;
        }
        try {
            return PaymentMethod.valueOf(value.toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PaymentMethod.OTHER;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }

    private LocalDate parseLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private static class PackageAccumulator {
        private Long packageId;
        private final List<PaymentPackageActivity> activities = new ArrayList<>();
        private final Set<Long> activityIds = new HashSet<>();

        private PackageAccumulator(Long packageId) {
            this.packageId = packageId;
        }
    }
}
