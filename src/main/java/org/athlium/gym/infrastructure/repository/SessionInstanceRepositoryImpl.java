package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;
import org.athlium.gym.infrastructure.mapper.SessionInstanceMapper;
import org.athlium.shared.domain.PageResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SessionInstanceRepositoryImpl implements SessionInstanceRepository {

    @Inject
    SessionInstancePanacheRepository panacheRepository;

    @Inject
    SessionInstanceMapper mapper;

    @Override
    public SessionInstance save(SessionInstance sessionInstance) {
        SessionInstanceEntity entity = mapper.toEntity(sessionInstance);
        if (sessionInstance.getId() != null) {
            entity.id = sessionInstance.getId();
        }
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public boolean existsByOrganizationAndHeadquartersAndActivityAndStartsAt(
            Long organizationId,
            Long headquartersId,
            Long activityId,
            Instant startsAt
    ) {
        return panacheRepository.count(
                "organizationId = ?1 and headquartersId = ?2 and activityId = ?3 and startsAt = ?4",
                organizationId,
                headquartersId,
                activityId,
                startsAt
        ) > 0;
    }

    @Override
    public Optional<SessionInstance> findById(Long id) {
        SessionInstanceEntity entity = panacheRepository.findById(id);
        return Optional.ofNullable(entity).map(mapper::toDomain);
    }

    @Override
    public Optional<SessionInstance> findByIdForUpdate(Long id) {
        SessionInstanceEntity entity = panacheRepository.findById(id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(entity).map(mapper::toDomain);
    }

    @Override
    public PageResponse<SessionInstance> findSessions(
            Long organizationId,
            Long headquartersId,
            Long activityId,
            SessionStatus status,
            Instant from,
            Instant to,
            int page,
            int size,
            boolean sortAscending
    ) {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();

        appendCondition(queryBuilder, params, "organizationId =", organizationId);
        appendCondition(queryBuilder, params, "headquartersId =", headquartersId);
        appendCondition(queryBuilder, params, "activityId =", activityId);
        appendCondition(queryBuilder, params, "status =", status);

        if (from != null) {
            appendCondition(queryBuilder, params, "startsAt >=", from);
        }
        if (to != null) {
            appendCondition(queryBuilder, params, "startsAt <=", to);
        }

        String orderBy = " order by startsAt " + (sortAscending ? "asc" : "desc");

        PanacheQuery<SessionInstanceEntity> query;
        if (params.isEmpty()) {
            query = panacheRepository.find("from SessionInstanceEntity" + orderBy);
        } else {
            query = panacheRepository.find(queryBuilder + orderBy, params.toArray());
        }

        query.page(Page.of(page, size));

        List<SessionInstance> sessions = query.list().stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResponse<>(sessions, page, size, query.count());
    }

    private void appendCondition(StringBuilder queryBuilder, List<Object> params, String expression, Object value) {
        if (value == null) {
            return;
        }

        if (!queryBuilder.isEmpty()) {
            queryBuilder.append(" and ");
        }

        queryBuilder.append(expression).append(" ?").append(params.size() + 1);
        params.add(value);
    }
}
