package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class DeleteHeadquartersUseCase {

    @Inject
    HeadquartersRepository headquartersRepository;

    @Transactional
    public void execute(Long id) {
        if (!headquartersRepository.existsById(id)) {
            throw new EntityNotFoundException("Headquarters not found with id: " + id);
        }
        headquartersRepository.deleteById(id);
    }
}