package org.athlium.gym.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Activity {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Long hqId;

    public static Activity createNew(String name, String description, Long hqId) {
        return Activity.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .hqId(hqId)
                .build();
    }
}
