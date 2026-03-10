package org.athlium.gym.presentation.mapper;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionParticipant;
import org.athlium.gym.presentation.dto.ActivityResponse;
import org.athlium.gym.presentation.dto.SessionParticipantResponse;
import org.athlium.gym.presentation.dto.SessionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "jakarta-cdi")
public interface SessionDtoMapper {
    SessionResponse toResponse(SessionInstance session);

    List<SessionResponse> toResponseList(List<SessionInstance> sessions);

    default ActivityResponse toActivityResponse(Activity activity) {
        if (activity == null) {
            return null;
        }

        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setName(activity.getName());
        response.setDescription(activity.getDescription());
        response.setIsActive(activity.getIsActive());
        response.setHqId(activity.getHqId());
        return response;
    }

    default List<SessionParticipantResponse> toParticipantResponseList(List<SessionParticipant> participants) {
        if (participants == null || participants.isEmpty()) {
            return List.of();
        }

        return participants.stream()
                .map(this::toParticipantResponse)
                .toList();
    }

    default SessionParticipantResponse toParticipantResponse(SessionParticipant participant) {
        SessionParticipantResponse response = new SessionParticipantResponse();
        response.setId(participant.getId());
        response.setName(participant.getName());
        response.setLastName(participant.getLastName());
        response.setEmail(participant.getEmail());
        return response;
    }
}
