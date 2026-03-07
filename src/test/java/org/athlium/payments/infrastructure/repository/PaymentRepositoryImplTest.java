package org.athlium.payments.infrastructure.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentMethod;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRepositoryImplTest {

    @Test
    void shouldReturnMappedPaymentsAndApplyAllProvidedFilters() {
        QuerySpec countQuery = QuerySpec.forCount(1L);
        QuerySpec dataQuery = QuerySpec.forRows(Collections.singletonList(new Object[] {
                9L,
                new BigDecimal("45.50"),
                "CARD",
                LocalDate.of(2026, 1, 5),
                "Ana",
                "Lopez",
                88L,
                3L,
                1L
        }));
        QuerySpec activitiesQuery = QuerySpec.forRows(Collections.singletonList(new Object[] {
                9L,
                21L,
                "Yoga",
                "Mind and body",
                true,
                3L
        }));

        FakeEntityManager fakeEntityManager = new FakeEntityManager(List.of(countQuery, dataQuery, activitiesQuery));
        PaymentRepositoryImpl repository = new PaymentRepositoryImpl();
        repository.em = fakeEntityManager.proxy();

        PaymentSearchCriteria criteria = new PaymentSearchCriteria(
                "ana",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                88L,
                PaymentMethod.CARD,
                new BigDecimal("10.00"),
                new BigDecimal("90.00"),
                3L,
                1L,
                2,
                5,
                "amount",
                true
        );

        var page = repository.findPayments(criteria);

        assertEquals(1, page.getContent().size());
        PaymentListItem item = page.getContent().getFirst();
        assertEquals(9L, item.getId());
        assertEquals(new BigDecimal("45.50"), item.getAmount());
        assertEquals("CARD", item.getPaymentMethod().name());
        assertEquals(LocalDate.of(2026, 1, 5), item.getPaidAt());
        assertEquals("Ana", item.getUserName());
        assertEquals("Lopez", item.getUserLastName());
        assertEquals(1, item.getActivities().size());
        assertEquals("Yoga", item.getActivities().getFirst().getName());
        assertEquals(88L, item.getClientId());
        assertEquals(3L, item.getHeadquartersId());
        assertEquals(1L, item.getOrganizationId());
        assertEquals(1, page.getTotalElements());
        assertEquals(2, page.getPage());

        String countSql = fakeEntityManager.sqlStatements.getFirst();
        String dataSql = fakeEntityManager.sqlStatements.get(1);
        assertTrue(countSql.contains("LOWER(u.name) LIKE :player"));
        assertTrue(countSql.contains("p.client_id = :clientId"));
        assertTrue(countSql.contains("p.headquarters_id = :headquartersId"));
        assertTrue(countSql.contains("p.organization_id = :organizationId"));
        assertTrue(dataSql.contains("ORDER BY p.amount ASC, p.id DESC"));

        assertEquals("%ana%", countQuery.params.get("player"));
        assertEquals(LocalDate.of(2026, 1, 1), countQuery.params.get("paidAtFrom"));
        assertEquals(LocalDate.of(2026, 1, 31), countQuery.params.get("paidAtTo"));
        assertEquals(88L, countQuery.params.get("clientId"));
        assertEquals("CARD", countQuery.params.get("paymentMethod"));
        assertEquals(new BigDecimal("10.00"), countQuery.params.get("amountMin"));
        assertEquals(new BigDecimal("90.00"), countQuery.params.get("amountMax"));
        assertEquals(3L, countQuery.params.get("headquartersId"));
        assertEquals(1L, countQuery.params.get("organizationId"));
        assertEquals(5, dataQuery.params.get("size"));
        assertEquals(10, dataQuery.params.get("offset"));
        assertEquals(List.of(9L), activitiesQuery.params.get("paymentIds"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoPaymentsMatchFilters() {
        QuerySpec countQuery = QuerySpec.forCount(0L);
        FakeEntityManager fakeEntityManager = new FakeEntityManager(List.of(countQuery));

        PaymentRepositoryImpl repository = new PaymentRepositoryImpl();
        repository.em = fakeEntityManager.proxy();

        PaymentSearchCriteria criteria = new PaymentSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                20,
                "paid_at",
                false
        );

        var page = repository.findPayments(criteria);

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
        assertEquals(1, fakeEntityManager.sqlStatements.size());
        assertFalse(fakeEntityManager.sqlStatements.getFirst().contains("LIMIT :size OFFSET :offset"));
    }

    private record QuerySpec(Object singleResult, List<Object[]> rows, Map<String, Object> params) {

        static QuerySpec forCount(Object singleResult) {
            return new QuerySpec(singleResult, List.of(), new HashMap<>());
        }

        static QuerySpec forRows(List<Object[]> rows) {
            return new QuerySpec(null, rows, new HashMap<>());
        }
    }

    private static class FakeEntityManager implements InvocationHandler {
        private final List<QuerySpec> specs;
        private int queryIndex;
        private final List<String> sqlStatements = new ArrayList<>();

        private FakeEntityManager(List<QuerySpec> specs) {
            this.specs = specs;
        }

        EntityManager proxy() {
            return (EntityManager) Proxy.newProxyInstance(
                    EntityManager.class.getClassLoader(),
                    new Class<?>[] { EntityManager.class },
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("createNativeQuery".equals(method.getName())) {
                String sql = (String) args[0];
                sqlStatements.add(sql);
                QuerySpec spec = specs.get(queryIndex++);
                return createQueryProxy(spec);
            }
            throw new UnsupportedOperationException("Unsupported EntityManager method: " + method.getName());
        }

        private Query createQueryProxy(QuerySpec spec) {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setParameter" -> {
                        spec.params.put((String) args[0], args[1]);
                        return proxy;
                    }
                    case "getSingleResult" -> {
                        return spec.singleResult;
                    }
                    case "getResultList" -> {
                        return spec.rows;
                    }
                    default -> throw new UnsupportedOperationException("Unsupported Query method: " + method.getName());
                }
            };

            return (Query) Proxy.newProxyInstance(
                    Query.class.getClassLoader(),
                    new Class<?>[] { Query.class },
                    handler);
        }
    }
}
