package org.athlium.clients.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UpdateActiveClientPackageUseCase {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Transactional
    public ClientPackage execute(Long userId, Long packageId, List<ClientPackageCredit> credits) {
        validateUserId(userId);
        validatePackageId(packageId);
        validateCredits(credits);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);

        ClientPackage existing = clientPackageRepository.findByIdForUpdate(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Client package", packageId));

        if (!userId.equals(existing.getUserId())) {
            throw new BadRequestException("Package does not belong to the user");
        }

        if (!Boolean.TRUE.equals(existing.getActive())) {
            throw new BadRequestException("Package is not active");
        }

        if (existing.isExpired(today)) {
            existing.deactivate();
            clientPackageRepository.save(existing);
            throw new BadRequestException("Active package already expired. Create a new package");
        }

        existing.setCredits(applyPatch(existing.getCredits(), credits));
        return clientPackageRepository.save(existing);
    }

    private List<ClientPackageCredit> applyPatch(List<ClientPackageCredit> existingCredits, List<ClientPackageCredit> patchCredits) {
        Map<Long, ClientPackageCredit> merged = new LinkedHashMap<>();

        if (existingCredits != null) {
            for (ClientPackageCredit existing : existingCredits) {
                merged.put(existing.getActivityId(), copy(existing));
            }
        }

        for (ClientPackageCredit patch : patchCredits) {
            merged.put(patch.getActivityId(), copy(patch));
        }

        List<ClientPackageCredit> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparing(ClientPackageCredit::getActivityId));
        return result;
    }

    private ClientPackageCredit copy(ClientPackageCredit credit) {
        ClientPackageCredit clone = new ClientPackageCredit();
        clone.setActivityId(credit.getActivityId());
        clone.setTokens(credit.getTokens());
        return clone;
    }

    private void validatePackageId(Long packageId) {
        if (packageId == null || packageId <= 0) {
            throw new BadRequestException("packageId must be a positive number");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
    }

    private void validateCredits(List<ClientPackageCredit> credits) {
        if (credits == null || credits.isEmpty()) {
            throw new BadRequestException("At least one activity credit is required");
        }
        for (ClientPackageCredit credit : credits) {
            if (credit.getActivityId() == null || credit.getActivityId() <= 0) {
                throw new BadRequestException("activityId must be a positive number");
            }
            if (credit.getTokens() == null || credit.getTokens() <= 0) {
                throw new BadRequestException("tokens must be greater than 0");
            }
        }
    }
}
