package org.athlium.gym.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;

import java.util.List;

@ApplicationScoped
public class GetAllHeadquartersUseCase {

    @Inject
    HeadquartersRepository headquartersRepository;

    public List<Headquarters> execute() {
        return headquartersRepository.findAll();
    }
}