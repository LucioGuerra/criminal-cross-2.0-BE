package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;

import java.time.Instant;

@ApplicationScoped
public class GetSessionsUseCase {

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    public PageResponse<SessionInstance> execute(
            Long organizationId,
            Long headquartersId,
            Long activityId,
            SessionStatus status,
            Instant from,
            Instant to,
            int page,
            int limit,
            String sort
    ) {
        if (page < 1) {
            throw new BadRequestException("Page must be greater than or equal to 1");
        }
        if (limit < 1 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be less than or equal to to");
        }

        boolean sortAscending = parseSort(sort);

        return sessionInstanceRepository.findSessions(
                organizationId,
                headquartersId,
                activityId,
                status,
                from,
                to,
                page - 1,
                limit,
                sortAscending
        );
    }

    private boolean parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return true;
        }

        String normalized = sort.trim().toLowerCase();
        if ("startsat:asc".equals(normalized)) {
            return true;
        }
        if ("startsat:desc".equals(normalized)) {
            return false;
        }

        throw new BadRequestException("sort must be startsAt:asc or startsAt:desc");
    }
}
