package org.athlium.clients.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.repository.ClientPackageRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;

@ApplicationScoped
public class ExpireExpiredClientPackagesUseCase {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Transactional
    public long execute() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return clientPackageRepository.deactivateExpiredPackages(today);
    }
}
