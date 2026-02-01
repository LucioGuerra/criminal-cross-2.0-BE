package org.athlium.gym.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.repository.HeadquartersRepository;
import org.athlium.gym.infrastructure.mapper.HeadquartersMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class HeadquartersRepositoryImpl implements HeadquartersRepository {

    @Inject
    HeadquartersPanacheRepository panacheRepository;

    @Inject
    HeadquartersMapper mapper;

    @Override
    @Transactional
    public Headquarters save(Headquarters headquarters) {
        var entity = mapper.toEntity(headquarters);
        if (headquarters.getId() != null) {
            entity.id = headquarters.getId();
        }
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Headquarters> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Headquarters> findAll() {
        return panacheRepository.listAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Headquarters> findByOrganizationId(Long organizationId) {
        return panacheRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        panacheRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return panacheRepository.findByIdOptional(id).isPresent();
    }
}