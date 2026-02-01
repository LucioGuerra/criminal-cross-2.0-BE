package org.athlium.gym.infrastructure.mapper;

import org.athlium.gym.domain.model.Organization;
import org.athlium.gym.infrastructure.entity.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jakarta-cdi", uses = HeadquartersMapper.class)
public interface OrganizationMapper {

    @Mapping(source = "headquarters", target = "headQuarters")
    Organization toDomain(OrganizationEntity entity);

    @Mapping(target = "headquarters", ignore = true)
    OrganizationEntity toEntity(Organization domain);
}