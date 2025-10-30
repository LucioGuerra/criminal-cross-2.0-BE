package org.athlium.gym.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import org.athlium.gym.domain.model.Activity;
import org.athlium.shared.domain.PageResponse;

import java.util.List;

public interface ActivityRepository {
    Activity save(Activity activity);
    Activity findById(Long id);
    Activity update(Activity activity);
    void delete(Long id);
    PageResponse<Activity> findByName(String name, Page page);
    PageResponse<Activity> findPagedByTenantId(Long tenantId, Boolean isActive, Page page);
    List<Activity> findAllByTenantId(Long tenantId, Boolean isActive);
}
