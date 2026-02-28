package org.athlium.clients.domain.repository;

import org.athlium.clients.domain.model.ClientPackage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClientPackageRepository {

    ClientPackage save(ClientPackage clientPackage);

    List<ClientPackage> findActiveByUserId(Long userId);

    List<ClientPackage> findActiveByUserIdForUpdate(Long userId);

    Optional<ClientPackage> findByIdForUpdate(Long packageId);

    List<ClientPackage> findByUserId(Long userId);

    long deactivateExpiredPackagesByUserId(Long userId, LocalDate today);

    long deactivateExpiredPackages(LocalDate today);
}
