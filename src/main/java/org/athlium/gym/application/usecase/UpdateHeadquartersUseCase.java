package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class UpdateHeadquartersUseCase {

    @Inject
    HeadquartersRepository headquartersRepository;

    @Transactional
    public Headquarters execute(Long id, Headquarters headquarters) {
        if (!headquartersRepository.existsById(id)) {
            throw new EntityNotFoundException("Headquarters not found with id: " + id);
        }
        headquarters.setId(id);
        return headquartersRepository.save(headquarters);
    }
}