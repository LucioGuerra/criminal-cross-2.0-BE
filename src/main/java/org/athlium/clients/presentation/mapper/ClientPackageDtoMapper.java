package org.athlium.clients.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.presentation.dto.ClientPackageCreditResponse;
import org.athlium.clients.presentation.dto.ClientPackageResponse;
import org.athlium.clients.presentation.dto.ClientPackageUpsertRequest;
import org.athlium.shared.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClientPackageDtoMapper {

    public List<ClientPackageCredit> toCredits(ClientPackageUpsertRequest request) {
        if (request == null || request.getActivityTokens() == null || request.getActivityTokens().isEmpty()) {
            return List.of();
        }

        List<ClientPackageCredit> credits = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : request.getActivityTokens().entrySet()) {
            Long activityId = parseActivityId(entry.getKey());
            Integer tokens = entry.getValue();

            ClientPackageCredit credit = new ClientPackageCredit();
            credit.setActivityId(activityId);
            credit.setTokens(tokens);
            credits.add(credit);
        }

        credits.sort(Comparator.comparing(ClientPackageCredit::getActivityId));
        return credits;
    }

    public ClientPackageResponse toResponse(ClientPackage clientPackage) {
        ClientPackageResponse response = new ClientPackageResponse();
        response.setId(clientPackage.getId());
        response.setUserId(clientPackage.getUserId());
        response.setPaymentId(clientPackage.getPaymentId());
        response.setActive(clientPackage.getActive());
        response.setPeriodStart(clientPackage.getPeriodStart() != null ? clientPackage.getPeriodStart().toString() : null);
        response.setPeriodEnd(clientPackage.getPeriodEnd() != null ? clientPackage.getPeriodEnd().toString() : null);

        List<ClientPackageCreditResponse> credits = clientPackage.getCredits().stream().map(credit -> {
            ClientPackageCreditResponse creditResponse = new ClientPackageCreditResponse();
            creditResponse.setActivityId(credit.getActivityId());
            creditResponse.setTokens(credit.getTokens());
            return creditResponse;
        }).toList();

        response.setCredits(credits);
        return response;
    }

    public List<ClientPackageResponse> toResponseList(List<ClientPackage> packages) {
        return packages.stream().map(this::toResponse).toList();
    }

    private Long parseActivityId(String rawActivityId) {
        if (rawActivityId == null || rawActivityId.isBlank()) {
            throw new BadRequestException("activityId key cannot be blank");
        }
        try {
            Long activityId = Long.parseLong(rawActivityId.trim());
            if (activityId <= 0) {
                throw new BadRequestException("activityId must be a positive number");
            }
            return activityId;
        } catch (NumberFormatException ex) {
            throw new BadRequestException("activityId keys must be numeric");
        }
    }
}
