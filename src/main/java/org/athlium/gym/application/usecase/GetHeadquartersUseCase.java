package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class GetHeadquartersUseCase {

    @Inject
    HeadquartersRepository headquartersRepository;

    public Headquarters execute(Long id) {
        return headquartersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Headquarters not found with id: " + id));
    }
}