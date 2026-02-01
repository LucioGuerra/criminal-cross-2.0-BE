package org.athlium.users.infrastructure.mapper;

import org.athlium.users.domain.model.User;
import org.athlium.users.infrastructure.dto.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta-cdi")
public interface UserDtoMapper {

    UserResponseDto toResponseDto(User user);
}