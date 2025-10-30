package org.athlium.gym.domain.repository;

import io.quarkus.panache.common.Page;
import org.athlium.gym.domain.model.Activity;
import org.athlium.shared.domain.PageResponse;

import java.util.List;

public interface ActivityRepository {
    Activity save(Activity activity);
    Activity findById(Long id);
    Activity update(Activity activity);
    void delete(Long id);
    PageResponse<Activity> findPagedByHqId(Long hqId, Boolean isActive, Page page);
    List<Activity> findAllByHqId(Long hqId, Boolean isActive);
    PageResponse<Activity> findByNameAndHqId(String name, Long hqId, Page page);
}
