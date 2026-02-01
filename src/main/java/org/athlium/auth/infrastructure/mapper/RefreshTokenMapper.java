package org.athlium.auth.infrastructure.mapper;

import org.athlium.auth.domain.model.RefreshToken;
import org.athlium.auth.infrastructure.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for RefreshToken domain model and entity conversions.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface RefreshTokenMapper {

    RefreshToken toDomain(RefreshTokenEntity entity);

    RefreshTokenEntity toEntity(RefreshToken domain);
}
