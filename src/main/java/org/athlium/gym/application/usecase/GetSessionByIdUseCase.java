package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.repository.SessionInstanceRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;

@ApplicationScoped
public class GetSessionByIdUseCase {

    @Inject
    SessionInstanceRepository sessionInstanceRepository;

    public SessionInstance execute(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Session ID must be a positive number");
        }

        return sessionInstanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session", id));
    }
}
