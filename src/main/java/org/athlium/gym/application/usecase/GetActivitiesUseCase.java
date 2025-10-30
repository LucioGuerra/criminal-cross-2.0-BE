package org.athlium.gym.application.usecase;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;

import java.util.List;

@ApplicationScoped
public class GetActivitiesUseCase {

    @Inject
    ActivityRepository activityRepository;

    public PageResponse<Activity> executeByTenant(Long tenantId, Boolean isActive, int page, int size) {
        if (tenantId == null) {
            throw new BadRequestException("Tenant ID is required");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid pagination parameters");
        }
        
        return activityRepository.findPagedByTenantId(tenantId, isActive, Page.of(page, size));
    }

    public List<Activity> executeAllByTenant(Long tenantId, Boolean isActive) {
        if (tenantId == null) {
            throw new BadRequestException("Tenant ID is required");
        }
        
        return activityRepository.findAllByTenantId(tenantId, isActive);
    }

    public PageResponse<Activity> executeByName(String name, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Activity name is required");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid pagination parameters");
        }
        
        return activityRepository.findByName(name, Page.of(page, size));
    }
}