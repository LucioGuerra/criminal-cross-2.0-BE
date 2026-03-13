package org.athlium.users.domain.repository;

import org.athlium.shared.domain.PageResponse;
import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;

import java.util.List;

public interface UserQueryRepository {

    PageResponse<UserWithPackageStatus> findUsersByHeadquarters(Long headquartersId, String status,
            String search, int page, int size, String sort);

    PageResponse<UserWithPackageStatus> findUsersByOrganization(Long organizationId, String status,
            String search, int page, int size, String sort);

    List<UserHqMembership> findHqMembershipsByUserIds(List<Long> userIds);

    PageResponse<UserWithPackageStatus> findAllUsers(String status, String search,
            int page, int size, String sort);

    UserWithPackageStatus findUserById(Long userId);
}
