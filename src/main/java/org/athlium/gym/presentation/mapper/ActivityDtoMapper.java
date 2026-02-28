package org.athlium.gym.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.presentation.dto.ActivityInput;
import org.athlium.gym.presentation.dto.ActivityPageResponse;
import org.athlium.gym.presentation.dto.ActivityResponse;
import org.athlium.gym.presentation.dto.ActivityUpdateInput;
import org.athlium.shared.domain.PageResponse;

import java.util.List;

@Mapper(componentModel = "jakarta-cdi")
public interface ActivityDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Activity toDomain(ActivityInput input);

    @Mapping(target = "hqId", ignore = true)
    Activity toDomain(ActivityUpdateInput input);

    ActivityResponse toResponse(Activity activity);

    List<ActivityResponse> toResponseList(List<Activity> activities);

    @Mapping(target = "content", source = "content")
    ActivityPageResponse toPageResponse(PageResponse<Activity> pageResponse);
}
