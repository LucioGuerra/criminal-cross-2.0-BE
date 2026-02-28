package org.athlium.clients.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.shared.exception.BadRequestException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class GetActiveClientPackageUseCase {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Transactional
    public List<ClientPackage> execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);
        return clientPackageRepository.findActiveByUserId(userId);
    }
}
