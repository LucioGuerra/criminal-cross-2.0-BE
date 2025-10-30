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

    public PageResponse<Activity> executeByHeadquarter(Long hqId, Boolean isActive, int page, int size) {
        if (hqId == null) {
            throw new BadRequestException("Headquarter ID is required");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid pagination parameters");
        }
        
        return activityRepository.findPagedByHqId(hqId, isActive, Page.of(page, size));
    }

    public List<Activity> executeAllByHeadquarter(Long hqId, Boolean isActive) {
        if (hqId == null) {
            throw new BadRequestException("Headquarter ID is required");
        }
        
        return activityRepository.findAllByHqId(hqId, isActive);
    }

    public PageResponse<Activity> executeByName(String name, Long hqId, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Activity name is required");
        }
        if (hqId == null) {
            throw new BadRequestException("Headquarter ID is required");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Invalid pagination parameters");
        }
        
        return activityRepository.findByNameAndHqId(name, hqId, Page.of(page, size));
    }
}