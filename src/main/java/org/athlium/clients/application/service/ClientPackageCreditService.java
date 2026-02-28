package org.athlium.clients.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ClientPackageCreditService {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Transactional
    public boolean hasAvailableCredit(Long userId, Long activityId) {
        validateIds(userId, activityId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);

        List<ClientPackage> activePackages = clientPackageRepository.findActiveByUserId(userId);
        for (ClientPackage clientPackage : activePackages) {
            if (findCredit(clientPackage, activityId)
                    .map(credit -> credit.getTokens() != null && credit.getTokens() > 0)
                    .orElse(false)) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public Long consumeCredit(Long userId, Long activityId) {
        validateIds(userId, activityId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);

        List<ClientPackage> activePackages = new ArrayList<>(clientPackageRepository.findActiveByUserIdForUpdate(userId));
        activePackages.sort(Comparator
                .comparing(ClientPackage::getPeriodEnd, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ClientPackage::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        for (ClientPackage clientPackage : activePackages) {
            var creditOpt = findCredit(clientPackage, activityId);
            if (creditOpt.isPresent()) {
                ClientPackageCredit credit = creditOpt.get();
                Integer tokens = credit.getTokens();
                if (tokens != null && tokens > 0) {
                    credit.setTokens(tokens - 1);
                    clientPackageRepository.save(clientPackage);
                    return clientPackage.getId();
                }
            }
        }

        throw new BadRequestException("User has no available credits for this activity");
    }

    @Transactional
    public void refundCredit(Long userId, Long activityId, Long consumedPackageId) {
        validateIds(userId, activityId);

        if (consumedPackageId != null) {
            ClientPackage consumedPackage = clientPackageRepository.findByIdForUpdate(consumedPackageId)
                    .orElseThrow(() -> new EntityNotFoundException("Client package", consumedPackageId));
            if (!userId.equals(consumedPackage.getUserId())) {
                throw new BadRequestException("Consumed package does not belong to booking user");
            }
            incrementCredit(consumedPackage, activityId);
            clientPackageRepository.save(consumedPackage);
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);
        List<ClientPackage> activePackages = new ArrayList<>(clientPackageRepository.findActiveByUserIdForUpdate(userId));
        activePackages.sort(Comparator
                .comparing(ClientPackage::getPeriodEnd, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ClientPackage::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        for (ClientPackage clientPackage : activePackages) {
            if (findCredit(clientPackage, activityId).isPresent()) {
                incrementCredit(clientPackage, activityId);
                clientPackageRepository.save(clientPackage);
                return;
            }
        }

        throw new BadRequestException("No package found to refund credit for this activity");
    }

    private java.util.Optional<ClientPackageCredit> findCredit(ClientPackage clientPackage, Long activityId) {
        if (clientPackage.getCredits() == null) {
            return java.util.Optional.empty();
        }
        return clientPackage.getCredits().stream()
                .filter(credit -> activityId.equals(credit.getActivityId()))
                .findFirst();
    }

    private void incrementCredit(ClientPackage clientPackage, Long activityId) {
        List<ClientPackageCredit> credits = clientPackage.getCredits();
        if (credits == null) {
            credits = new ArrayList<>();
            clientPackage.setCredits(credits);
        }

        var existing = findCredit(clientPackage, activityId);
        if (existing.isPresent()) {
            Integer current = existing.get().getTokens() == null ? 0 : existing.get().getTokens();
            existing.get().setTokens(current + 1);
            return;
        }

        ClientPackageCredit credit = new ClientPackageCredit();
        credit.setActivityId(activityId);
        credit.setTokens(1);
        credits.add(credit);
    }

    private void validateIds(Long userId, Long activityId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
        if (activityId == null || activityId <= 0) {
            throw new BadRequestException("activityId must be a positive number");
        }
    }
}
