package org.athlium.users.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.athlium.shared.domain.PageResponse;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.domain.repository.UserQueryRepository;

@ApplicationScoped
public class GetUsersByOrgUseCase {

    @Inject
    UserQueryRepository userQueryRepository;

    public PageResponse<UserWithPackageStatus> execute(Long organizationId, String status,
            String search, int page, int size, String sort) {

        if (organizationId == null) {
            throw new BadRequestException("Organization ID must not be null");
        }
        if (page < 1) {
            throw new BadRequestException("Page must be >= 1");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }
        if (status != null && !status.isBlank()) {
            PackageStatus.fromString(status);
        }
        if (sort == null || sort.isBlank()) {
            sort = "name:asc";
        }

        return userQueryRepository.findUsersByOrganization(organizationId, status, search, page - 1, size, sort);
    }
}
