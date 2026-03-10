package org.athlium.users.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.shared.exception.DomainException;
import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.User;

import java.util.Set;

@ApplicationScoped
public class HeadquartersMembershipAuthorizationService {

    @Inject
    HeadquartersRepository headquartersRepository;

    public Headquarters authorizeMembershipUpdate(String targetFirebaseUid, Long headquartersId, User currentUser) {
        Headquarters targetHeadquarters = headquartersRepository.findById(headquartersId)
                .orElseThrow(() -> new DomainException("Headquarters not found"));

        if (isSelfUpdate(targetFirebaseUid, currentUser)) {
            return targetHeadquarters;
        }

        if (currentUser.hasRole(Role.SUPERADMIN)) {
            return targetHeadquarters;
        }

        if (!currentUser.hasRole(Role.ORG_ADMIN) && !currentUser.hasRole(Role.ORG_OWNER)) {
            throw new DomainException("Only headquarters admins, owners, or SUPERADMIN can update other users");
        }

        if (!belongsToOrganization(currentUser.getHeadquartersIds(), targetHeadquarters.getOrganizationId())) {
            throw new DomainException("You can only update users in your organization");
        }

        return targetHeadquarters;
    }

    private boolean isSelfUpdate(String targetFirebaseUid, User currentUser) {
        return currentUser.getFirebaseUid() != null && currentUser.getFirebaseUid().equals(targetFirebaseUid);
    }

    private boolean belongsToOrganization(Set<Long> currentUserHeadquartersIds, Long organizationId) {
        if (currentUserHeadquartersIds == null || currentUserHeadquartersIds.isEmpty()) {
            return false;
        }

        return currentUserHeadquartersIds.stream()
                .map(headquartersRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .anyMatch(headquarters -> organizationId.equals(headquarters.getOrganizationId()));
    }
}
