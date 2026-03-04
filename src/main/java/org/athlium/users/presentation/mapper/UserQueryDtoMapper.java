package org.athlium.users.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import org.athlium.users.domain.model.Role;
import org.athlium.users.domain.model.UserHqMembership;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.athlium.users.presentation.dto.UserHqMembershipResponse;
import org.athlium.users.presentation.dto.UserWithStatusResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class UserQueryDtoMapper {

    public UserWithStatusResponse toResponse(UserWithPackageStatus domain) {
        if (domain == null) {
            return null;
        }

        UserWithStatusResponse response = new UserWithStatusResponse();
        response.setId(domain.getId());
        response.setName(domain.getName());
        response.setLastName(domain.getLastName());
        response.setEmail(domain.getEmail());
        response.setActive(domain.getActive());
        response.setPackageStatus(domain.getPackageStatus() != null
                ? domain.getPackageStatus().name() : null);
        response.setPeriodEnd(domain.getPeriodEnd());
        response.setDaysRemaining(domain.getDaysRemaining());

        if (domain.getRoles() != null) {
            Set<String> roleStrings = new HashSet<>();
            for (Role role : domain.getRoles()) {
                roleStrings.add(role.name());
            }
            response.setRoles(roleStrings);
        }

        if (domain.getHqMemberships() != null) {
            List<UserHqMembershipResponse> membershipResponses = new ArrayList<>();
            for (UserHqMembership membership : domain.getHqMemberships()) {
                membershipResponses.add(toHqMembershipResponse(membership));
            }
            response.setHqMemberships(membershipResponses);
        }

        return response;
    }

    public UserHqMembershipResponse toHqMembershipResponse(UserHqMembership domain) {
        if (domain == null) {
            return null;
        }

        UserHqMembershipResponse response = new UserHqMembershipResponse();
        response.setId(domain.getHqId());
        response.setName(domain.getHqName());
        response.setPackageStatus(domain.getPackageStatus() != null
                ? domain.getPackageStatus().name() : null);
        response.setPeriodEnd(domain.getPeriodEnd());
        response.setDaysRemaining(domain.getDaysRemaining());
        return response;
    }

    public List<UserWithStatusResponse> toResponseList(List<UserWithPackageStatus> domains) {
        if (domains == null) {
            return null;
        }

        List<UserWithStatusResponse> responses = new ArrayList<>();
        for (UserWithPackageStatus domain : domains) {
            responses.add(toResponse(domain));
        }
        return responses;
    }
}
