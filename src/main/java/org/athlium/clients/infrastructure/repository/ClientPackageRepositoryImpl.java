package org.athlium.clients.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.clients.infrastructure.entity.ClientPackageCreditEntity;
import org.athlium.clients.infrastructure.entity.ClientPackageEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClientPackageRepositoryImpl implements ClientPackageRepository {

    @Inject
    ClientPackagePanacheRepository clientPackagePanacheRepository;

    @Override
    public ClientPackage save(ClientPackage clientPackage) {
        ClientPackageEntity entity;
        boolean isNew = false;
        if (clientPackage.getId() != null) {
            entity = clientPackagePanacheRepository.findById(clientPackage.getId());
            if (entity == null) {
                entity = new ClientPackageEntity();
                isNew = true;
            }
        } else {
            entity = new ClientPackageEntity();
            isNew = true;
        }

        entity.userId = clientPackage.getUserId();
        entity.paymentId = clientPackage.getPaymentId();
        entity.periodStart = clientPackage.getPeriodStart();
        entity.periodEnd = clientPackage.getPeriodEnd();
        entity.active = clientPackage.getActive();
        replaceCredits(entity, clientPackage.getCredits());

        if (isNew) {
            clientPackagePanacheRepository.persist(entity);
        }
        return toDomain(entity);
    }

    @Override
    public List<ClientPackage> findActiveByUserId(Long userId) {
        return clientPackagePanacheRepository
                .find("userId = ?1 and active = ?2", userId, true)
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ClientPackage> findActiveByUserIdForUpdate(Long userId) {
        return clientPackagePanacheRepository
                .find("userId = ?1 and active = ?2", userId, true)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ClientPackage> findByIdForUpdate(Long packageId) {
        ClientPackageEntity entity = clientPackagePanacheRepository
                .find("id", packageId)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<ClientPackage> findByUserId(Long userId) {
        return clientPackagePanacheRepository.find("userId", userId)
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long deactivateExpiredPackagesByUserId(Long userId, LocalDate today) {
        return clientPackagePanacheRepository.update(
                "active = false where userId = ?1 and active = true and periodEnd < ?2",
                userId,
                today
        );
    }

    @Override
    public long deactivateExpiredPackages(LocalDate today) {
        return clientPackagePanacheRepository.update("active = false where active = true and periodEnd < ?1", today);
    }

    private void replaceCredits(ClientPackageEntity entity, List<ClientPackageCredit> credits) {
        if (credits == null) {
            entity.credits.clear();
            return;
        }

        // Remove credits that are no longer present
        entity.credits.removeIf(existing -> credits.stream()
                .noneMatch(c -> c.getActivityId().equals(existing.activityId)));

        for (ClientPackageCredit credit : credits) {
            // Update existing or add new
            Optional<ClientPackageCreditEntity> existingOpt = entity.credits.stream()
                    .filter(c -> c.activityId.equals(credit.getActivityId()))
                    .findFirst();

            if (existingOpt.isPresent()) {
                existingOpt.get().tokens = credit.getTokens();
            } else {
                ClientPackageCreditEntity creditEntity = new ClientPackageCreditEntity();
                creditEntity.clientPackage = entity;
                creditEntity.activityId = credit.getActivityId();
                creditEntity.tokens = credit.getTokens();
                entity.credits.add(creditEntity);
            }
        }
    }

    private ClientPackage toDomain(ClientPackageEntity entity) {
        ClientPackage clientPackage = new ClientPackage();
        clientPackage.setId(entity.id);
        clientPackage.setUserId(entity.userId);
        clientPackage.setPaymentId(entity.paymentId);
        clientPackage.setPeriodStart(entity.periodStart);
        clientPackage.setPeriodEnd(entity.periodEnd);
        clientPackage.setActive(entity.active);
        clientPackage.setCreatedAt(entity.createdAt);
        clientPackage.setUpdatedAt(entity.updatedAt);

        List<ClientPackageCredit> credits = new ArrayList<>();
        if (entity.credits != null) {
            for (ClientPackageCreditEntity creditEntity : entity.credits) {
                ClientPackageCredit credit = new ClientPackageCredit();
                credit.setActivityId(creditEntity.activityId);
                credit.setTokens(creditEntity.tokens);
                credits.add(credit);
            }
        }
        clientPackage.setCredits(credits);
        return clientPackage;
    }
}
