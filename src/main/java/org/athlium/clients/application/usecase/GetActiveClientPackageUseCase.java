package org.athlium.clients.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.gym.domain.model.Activity;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.gym.domain.repository.ActivityRepository;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GetActiveClientPackageUseCase {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Inject
    ActivityRepository activityRepository;

    @Transactional
    public List<ClientPackage> execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);
        List<ClientPackage> packages = clientPackageRepository.findActiveByUserId(userId);
        enrichActivities(packages);
        return packages;
    }

    private void enrichActivities(List<ClientPackage> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }

        List<Long> activityIds = packages.stream()
                .flatMap(clientPackage -> safeCredits(clientPackage).stream())
                .map(ClientPackageCredit::getActivityId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Map<Long, Activity> activitiesById = activityRepository.findByIds(activityIds);
        packages.forEach(clientPackage -> safeCredits(clientPackage)
                .forEach(credit -> credit.setActivity(activitiesById.get(credit.getActivityId()))));
    }

    private List<ClientPackageCredit> safeCredits(ClientPackage clientPackage) {
        if (clientPackage.getCredits() == null) {
            return Collections.emptyList();
        }
        return clientPackage.getCredits();
    }
}
