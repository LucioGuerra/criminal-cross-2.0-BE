package org.athlium.gym.presentation.dto;

import lombok.Data;

@Data
public class SessionParticipantResponse {
    private Long id;
    private String name;
    private String lastName;
    private String email;
}
