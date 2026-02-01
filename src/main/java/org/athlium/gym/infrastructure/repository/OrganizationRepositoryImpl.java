package org.athlium.gym.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.domain.repository.OrganizationRepository;
import org.athlium.gym.infrastructure.mapper.OrganizationMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationRepositoryImpl implements OrganizationRepository {

    @Inject
    OrganizationPanacheRepository panacheRepository;

    @Inject
    OrganizationMapper mapper;

    @Override
    @Transactional
    public Organization save(Organization organization) {
        var entity = mapper.toEntity(organization);
        if (organization.getId() != null) {
            entity.id = organization.getId();
        }
        panacheRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Organization> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Organization> findAll() {
        return panacheRepository.listAll().stream()
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