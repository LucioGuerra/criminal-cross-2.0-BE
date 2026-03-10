package org.athlium.users.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.athlium.shared.domain.PageResponse;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.domain.repository.UserQueryRepository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "name", "name",
            "lastName", "last_name",
            "email", "email"
    );

    @Inject
    EntityManager em;

    @Override
    public PageResponse<UserWithPackageStatus> findUsersByHeadquarters(Long headquartersId, String status,
            String search, int page, int size, String sort) {

        StringBuilder countSql = new StringBuilder();
        countSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.id IS NULL THEN 4
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.id IS NULL THEN 'NO_PACKAGE'
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    JOIN user_headquarters uh ON uh.user_id = u.id AND uh.headquarters_id = :hqId
                    LEFT JOIN client_packages cp ON cp.user_id = u.id
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           package_status, period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC, period_end DESC
                           ) AS rn
                    FROM user_packages
                )
                SELECT COUNT(*) FROM best_status WHERE rn = 1
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("hqId", headquartersId);

        addStatusFilterToCount(countSql, params, status);
        addSearchFilterToCount(countSql, params, search);

        Query countQuery = em.createNativeQuery(countSql.toString());
        setParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0);
        }

        StringBuilder dataSql = new StringBuilder();
        dataSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.id IS NULL THEN 4
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.id IS NULL THEN 'NO_PACKAGE'
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    JOIN user_headquarters uh ON uh.user_id = u.id AND uh.headquarters_id = :hqId
                    LEFT JOIN client_packages cp ON cp.user_id = u.id
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           package_status, period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC, period_end DESC
                           ) AS rn
                    FROM user_packages
                )
                SELECT user_id, name, last_name, email, active,
                       package_status, period_end, days_remaining
                FROM best_status
                WHERE rn = 1
                """);

        Map<String, Object> dataParams = new HashMap<>();
        dataParams.put("hqId", headquartersId);

        addStatusFilter(dataSql, dataParams, status);
        addSearchFilter(dataSql, dataParams, search);

        dataSql.append(" ORDER BY ").append(buildSortClause(sort));
        dataSql.append(" LIMIT :size OFFSET :offset");
        dataParams.put("size", size);
        dataParams.put("offset", page * size);

        Query dataQuery = em.createNativeQuery(dataSql.toString());
        setParameters(dataQuery, dataParams);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<UserWithPackageStatus> users = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        for (Object[] row : rows) {
            UserWithPackageStatus user = mapRow(row);
            users.add(user);
            userIds.add(user.getId());
        }

        if (!userIds.isEmpty()) {
            Map<Long, Set<Role>> rolesMap = fetchRolesForUsers(userIds);
            Map<Long, Set<Long>> headquartersMap = fetchHeadquartersForUsers(userIds);
            for (UserWithPackageStatus user : users) {
                user.setRoles(rolesMap.getOrDefault(user.getId(), Collections.emptySet()));
                user.setHeadquartersIds(headquartersMap.getOrDefault(user.getId(), Collections.emptySet()));
            }
        }

        return new PageResponse<>(users, page, size, total);
    }

    @Override
    public PageResponse<UserWithPackageStatus> findUsersByOrganization(Long organizationId, String status,
            String search, int page, int size, String sort) {

        StringBuilder countSql = new StringBuilder();
        countSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    JOIN client_packages cp ON cp.user_id = u.id
                    JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    JOIN activity a ON a.id = cpc.activity_id
                    JOIN headquarters h ON h.id = a.hq_id
                    WHERE h.organization_id = :orgId
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           package_status, period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC, period_end DESC
                           ) AS rn
                    FROM user_packages
                )
                SELECT COUNT(*) FROM best_status WHERE rn = 1
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("orgId", organizationId);

        addStatusFilterToCount(countSql, params, status);
        addSearchFilterToCount(countSql, params, search);

        Query countQuery = em.createNativeQuery(countSql.toString());
        setParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0);
        }

        StringBuilder dataSql = new StringBuilder();
        dataSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    JOIN client_packages cp ON cp.user_id = u.id
                    JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    JOIN activity a ON a.id = cpc.activity_id
                    JOIN headquarters h ON h.id = a.hq_id
                    WHERE h.organization_id = :orgId
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           package_status, period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC, period_end DESC
                           ) AS rn
                    FROM user_packages
                )
                SELECT user_id, name, last_name, email, active,
                       package_status, period_end, days_remaining
                FROM best_status
                WHERE rn = 1
                """);

        Map<String, Object> dataParams = new HashMap<>();
        dataParams.put("orgId", organizationId);

        addStatusFilter(dataSql, dataParams, status);
        addSearchFilter(dataSql, dataParams, search);

        dataSql.append(" ORDER BY ").append(buildSortClause(sort));
        dataSql.append(" LIMIT :size OFFSET :offset");
        dataParams.put("size", size);
        dataParams.put("offset", page * size);

        Query dataQuery = em.createNativeQuery(dataSql.toString());
        setParameters(dataQuery, dataParams);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<UserWithPackageStatus> users = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        for (Object[] row : rows) {
            UserWithPackageStatus user = mapRow(row);
            users.add(user);
            userIds.add(user.getId());
        }

        if (!userIds.isEmpty()) {
            Map<Long, Set<Role>> rolesMap = fetchRolesForUsers(userIds);
            Map<Long, Set<Long>> headquartersMap = fetchHeadquartersForUsers(userIds);
            for (UserWithPackageStatus user : users) {
                user.setRoles(rolesMap.getOrDefault(user.getId(), Collections.emptySet()));
                user.setHeadquartersIds(headquartersMap.getOrDefault(user.getId(), Collections.emptySet()));
            }
        }

        return new PageResponse<>(users, page, size, total);
    }

    @Override
    public List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds, Long organizationId) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = """
                WITH user_hq_packages AS (
                    SELECT u.id AS user_id, h.id AS hq_id, h.name AS hq_name,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    JOIN client_packages cp ON cp.user_id = u.id
                    JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    JOIN activity a ON a.id = cpc.activity_id
                    JOIN headquarters h ON h.id = a.hq_id
                    WHERE h.organization_id = :orgId AND u.id IN (:userIds)
                ),
                best_hq_status AS (
                    SELECT user_id, hq_id, hq_name,
                           package_status, period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id, hq_id
                               ORDER BY status_rank ASC, period_end DESC
                           ) AS rn
                    FROM user_hq_packages
                )
                SELECT user_id, hq_id, hq_name, package_status, period_end, days_remaining
                FROM best_hq_status
                WHERE rn = 1
                ORDER BY user_id, hq_name
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("orgId", organizationId);
        query.setParameter("userIds", userIds);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<UserHqMembership> memberships = new ArrayList<>();

        for (Object[] row : rows) {
            memberships.add(mapHqMembershipRow(row));
        }

        return memberships;
    }

    @Override
    public PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
            int page, int size, String sort) {

        boolean filterNoPackage = status != null && status.equalsIgnoreCase("NO_PACKAGE");

        StringBuilder countSql = new StringBuilder();
        countSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    LEFT JOIN client_packages cp ON cp.user_id = u.id
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           COALESCE(package_status, 'NO_PACKAGE') AS package_status,
                           period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC NULLS LAST, period_end DESC NULLS LAST
                           ) AS rn
                    FROM user_packages
                )
                SELECT COUNT(*) FROM best_status WHERE rn = 1
                """);

        Map<String, Object> params = new HashMap<>();

        addStatusFilterToCount(countSql, params, status);
        addSearchFilterToCount(countSql, params, search);

        Query countQuery = em.createNativeQuery(countSql.toString());
        setParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), page, size, 0);
        }

        StringBuilder dataSql = new StringBuilder();
        dataSql.append("""
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    LEFT JOIN client_packages cp ON cp.user_id = u.id
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           COALESCE(package_status, 'NO_PACKAGE') AS package_status,
                           period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC NULLS LAST, period_end DESC NULLS LAST
                           ) AS rn
                    FROM user_packages
                )
                SELECT user_id, name, last_name, email, active,
                       package_status, period_end, days_remaining
                FROM best_status
                WHERE rn = 1
                """);

        Map<String, Object> dataParams = new HashMap<>();

        addStatusFilter(dataSql, dataParams, status);
        addSearchFilter(dataSql, dataParams, search);

        dataSql.append(" ORDER BY ").append(buildSortClause(sort));
        dataSql.append(" LIMIT :size OFFSET :offset");
        dataParams.put("size", size);
        dataParams.put("offset", page * size);

        Query dataQuery = em.createNativeQuery(dataSql.toString());
        setParameters(dataQuery, dataParams);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<UserWithPackageStatus> users = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();

        for (Object[] row : rows) {
            UserWithPackageStatus user = mapRow(row);
            users.add(user);
            userIds.add(user.getId());
        }

        if (!userIds.isEmpty()) {
            Map<Long, Set<Role>> rolesMap = fetchRolesForUsers(userIds);
            Map<Long, Set<Long>> headquartersMap = fetchHeadquartersForUsers(userIds);
            for (UserWithPackageStatus user : users) {
                user.setRoles(rolesMap.getOrDefault(user.getId(), Collections.emptySet()));
                user.setHeadquartersIds(headquartersMap.getOrDefault(user.getId(), Collections.emptySet()));
            }
        }

        return new PageResponse<>(users, page, size, total);
    }

    @Override
    public UserWithPackageStatus findUserById(Long userId) {
        String sql = """
                WITH user_packages AS (
                    SELECT DISTINCT u.id AS user_id, u.name, u.last_name, u.email, u.active,
                           cp.period_end,
                           CASE
                               WHEN cp.active = false THEN 3
                               WHEN cp.period_end < CURRENT_DATE THEN 3
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 2
                               ELSE 1
                           END AS status_rank,
                           CASE
                               WHEN cp.active = false THEN 'INACTIVE'
                               WHEN cp.period_end < CURRENT_DATE THEN 'INACTIVE'
                               WHEN cp.period_end <= CURRENT_DATE + 3 THEN 'EXPIRING'
                               ELSE 'ACTIVE'
                           END AS package_status
                    FROM users u
                    LEFT JOIN client_packages cp ON cp.user_id = u.id
                    LEFT JOIN client_package_credits cpc ON cpc.package_id = cp.id
                    LEFT JOIN activity a ON a.id = cpc.activity_id
                    WHERE u.id = :userId
                ),
                best_status AS (
                    SELECT user_id, name, last_name, email, active,
                           COALESCE(package_status, 'NO_PACKAGE') AS package_status,
                           period_end,
                           (period_end - CURRENT_DATE) AS days_remaining,
                           ROW_NUMBER() OVER (
                               PARTITION BY user_id
                               ORDER BY status_rank ASC NULLS LAST, period_end DESC NULLS LAST
                           ) AS rn
                    FROM user_packages
                )
                SELECT user_id, name, last_name, email, active,
                       package_status, period_end, days_remaining
                FROM best_status
                WHERE rn = 1
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", userId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        if (rows.isEmpty()) {
            return null;
        }

        UserWithPackageStatus user = mapRow(rows.get(0));

        Map<Long, Set<Role>> rolesMap = fetchRolesForUsers(List.of(userId));
        user.setRoles(rolesMap.getOrDefault(userId, Collections.emptySet()));
        Map<Long, Set<Long>> headquartersMap = fetchHeadquartersForUsers(List.of(userId));
        user.setHeadquartersIds(headquartersMap.getOrDefault(userId, Collections.emptySet()));

        return user;
    }

    // --- Private helper methods ---

    private UserWithPackageStatus mapRow(Object[] row) {
        UserWithPackageStatus user = new UserWithPackageStatus();
        user.setId(((Number) row[0]).longValue());
        user.setName((String) row[1]);
        user.setLastName((String) row[2]);
        user.setEmail((String) row[3]);
        user.setActive((Boolean) row[4]);
        user.setPackageStatus(PackageStatus.valueOf((String) row[5]));
        user.setPeriodEnd(row[6] != null ? ((java.sql.Date) row[6]).toLocalDate() : null);
        user.setDaysRemaining(row[7] != null ? ((Number) row[7]).intValue() : null);
        return user;
    }

    private UserHqMembership mapHqMembershipRow(Object[] row) {
        UserHqMembership membership = new UserHqMembership();
        membership.setUserId(((Number) row[0]).longValue());
        membership.setHqId(((Number) row[1]).longValue());
        membership.setHqName((String) row[2]);
        membership.setPackageStatus(PackageStatus.valueOf((String) row[3]));
        membership.setPeriodEnd(row[4] != null ? ((java.sql.Date) row[4]).toLocalDate() : null);
        membership.setDaysRemaining(row[5] != null ? ((Number) row[5]).intValue() : null);
        return membership;
    }

    private Map<Long, Set<Role>> fetchRolesForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = "SELECT ur.user_id, ur.role FROM user_roles ur WHERE ur.user_id IN (:userIds)";
        Query query = em.createNativeQuery(sql);
        query.setParameter("userIds", userIds);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        Map<Long, Set<Role>> rolesMap = new HashMap<>();

        for (Object[] row : rows) {
            Long userId = ((Number) row[0]).longValue();
            String roleName = (String) row[1];
            rolesMap.computeIfAbsent(userId, k -> new HashSet<>()).add(Role.valueOf(roleName));
        }

        return rolesMap;
    }

    private Map<Long, Set<Long>> fetchHeadquartersForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = "SELECT uh.user_id, uh.headquarters_id FROM user_headquarters uh WHERE uh.user_id IN (:userIds)";
        Query query = em.createNativeQuery(sql);
        query.setParameter("userIds", userIds);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        Map<Long, Set<Long>> headquartersMap = new HashMap<>();

        for (Object[] row : rows) {
            Long userId = ((Number) row[0]).longValue();
            Long headquartersId = ((Number) row[1]).longValue();
            headquartersMap.computeIfAbsent(userId, k -> new HashSet<>()).add(headquartersId);
        }

        return headquartersMap;
    }

    private String buildSortClause(String sort) {
        if (sort == null || sort.isBlank()) {
            return "name ASC";
        }

        String[] parts = sort.split(":");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toUpperCase() : "ASC";

        String sqlColumn = SORT_FIELD_MAP.get(field);
        if (sqlColumn == null) {
            return "name ASC";
        }

        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            direction = "ASC";
        }

        return sqlColumn + " " + direction;
    }

    private void addStatusFilter(StringBuilder sql, Map<String, Object> params, String status) {
        if (status != null && !status.isBlank()) {
            sql.append(" AND package_status = :status");
            params.put("status", status.toUpperCase());
        }
    }

    private void addSearchFilter(StringBuilder sql, Map<String, Object> params, String search) {
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE :search OR LOWER(last_name) LIKE :search OR LOWER(email) LIKE :search)");
            params.put("search", "%" + search.toLowerCase() + "%");
        }
    }

    private void addStatusFilterToCount(StringBuilder sql, Map<String, Object> params, String status) {
        if (status != null && !status.isBlank()) {
            sql.append(" AND package_status = :status");
            params.put("status", status.toUpperCase());
        }
    }

    private void addSearchFilterToCount(StringBuilder sql, Map<String, Object> params, String search) {
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE :search OR LOWER(last_name) LIKE :search OR LOWER(email) LIKE :search)");
            params.put("search", "%" + search.toLowerCase() + "%");
        }
    }

    private void setParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
