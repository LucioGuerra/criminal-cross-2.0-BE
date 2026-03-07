package org.athlium.gym.domain.repository;

import io.quarkus.panache.common.Page;
import org.athlium.gym.domain.model.Activity;
import org.athlium.shared.domain.PageResponse;

import java.util.List;
import java.util.Map;

public interface ActivityRepository {
    Activity save(Activity activity);
    Activity findById(Long id);
    Map<Long, Activity> findByIds(List<Long> ids);
    Activity update(Activity activity);
    void delete(Long id);
    PageResponse<Activity> findPagedByHqId(Long hqId, Boolean isActive, Page page);
    List<Activity> findAllByHqId(Long hqId, Boolean isActive);
    PageResponse<Activity> findByNameAndHqId(String name, Long hqId, Page page);
}
