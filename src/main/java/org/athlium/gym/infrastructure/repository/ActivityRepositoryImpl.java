package org.athlium.gym.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.gym.infrastructure.entity.ActivityEntity;
import org.athlium.gym.infrastructure.mapper.ActivityMapper;
import org.athlium.shared.domain.PageResponse;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActivityRepositoryImpl implements ActivityRepository {

    @Inject
    ActivityPanacheRepository panacheRepo;

    @Inject
    ActivityMapper mapper;

    @Override
    public Activity save(Activity activity) {
        ActivityEntity entity = mapper.toEntity(activity);
        panacheRepo.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Activity findById(Long id) {
        ActivityEntity entity = panacheRepo.findById(id);
        return mapper.toDomain(entity);
    }

    @Override
    public Activity update(Activity activity) {
        ActivityEntity entity = panacheRepo.findById(activity.getId());
        mapper.updateEntityFromDomain(activity, entity);
        panacheRepo.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public void delete(Long id) {
        panacheRepo.deleteById(id);
    }

    @Override
    public PageResponse<Activity> findByNameAndTenantId(String name, Long tenantId, Page page) {
        PanacheQuery<ActivityEntity> query = panacheRepo.find("LOWER(name) LIKE LOWER(?1) AND tenantId = ?2", "%" + name + "%", tenantId).page(page);
        List<Activity> activities = query.list().stream().map(mapper::toDomain).collect(Collectors.toList());
        long total = query.count();
        return new PageResponse<>(activities, page.index, page.size, total);
    }

    @Override
    public PageResponse<Activity> findPagedByTenantId(Long tenantId, Boolean isActive, Page page) {
        String queryStr = "tenantId = ?1" + (isActive != null ? " AND isActive = ?2" : "");
        Object[] params = isActive != null ? new Object[]{tenantId, isActive} : new Object[]{tenantId};

        var query = panacheRepo.find(queryStr, params).page(page);
        List<Activity> activities = query.list().stream().map(mapper::toDomain).collect(Collectors.toList());
        long total = query.count();

        return new PageResponse<>(activities, page.index, page.size, total);
    }

    @Override
    public List<Activity> findAllByTenantId(Long tenantId, Boolean isActive) {
        String queryStr = "tenantId = ?1" + (isActive != null ? " AND isActive = ?2" : "");
        Object[] params = isActive != null ? new Object[]{tenantId, isActive} : new Object[]{tenantId};

        return panacheRepo.find(queryStr, params)
                .list()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
