package org.athlium.users.infrastructure.mapper;

import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);

    void updateEntity(User user, @MappingTarget UserEntity entity);
}