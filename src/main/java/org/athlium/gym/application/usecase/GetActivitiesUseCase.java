package org.athlium.gym.application.usecase;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.domain.PageResponse;

import java.util.List;

@ApplicationScoped
public class GetActivitiesUseCase {

    @Inject
    ActivityRepository activityRepository;

    public PageResponse<Activity> executeByTenant(Long tenantId, Boolean isActive, int page, int size) {
        return activityRepository.findPagedByTenantId(tenantId, isActive, Page.of(page, size));
    }

    public List<Activity> executeAllByTenant(Long tenantId, Boolean isActive) {
        return activityRepository.findAllByTenantId(tenantId, isActive);
    }

    public PageResponse<Activity> executeByName(String name, int page, int size) {
        return activityRepository.findByName(name, Page.of(page, size));
    }
}