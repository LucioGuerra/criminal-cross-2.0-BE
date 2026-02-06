package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.presentation.dto.SessionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "jakarta-cdi")
public interface SessionDtoMapper {
    SessionResponse toResponse(SessionInstance session);

    List<SessionResponse> toResponseList(List<SessionInstance> sessions);
}
