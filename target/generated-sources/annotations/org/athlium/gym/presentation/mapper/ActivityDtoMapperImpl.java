package org.athlium.gym.presentation.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.presentation.dto.ActivityInput;
import org.athlium.gym.presentation.dto.ActivityPageResponse;
import org.athlium.gym.presentation.dto.ActivityResponse;
import org.athlium.gym.presentation.dto.ActivityUpdateInput;
import org.athlium.shared.domain.PageResponse;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-30T00:28:14-0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.8 (JetBrains s.r.o.)"
)
@ApplicationScoped
public class ActivityDtoMapperImpl implements ActivityDtoMapper {

    @Override
    public Activity toDomain(ActivityInput input) {
        if ( input == null ) {
            return null;
        }

        Activity.ActivityBuilder activity = Activity.builder();

        activity.name( input.getName() );
        activity.description( input.getDescription() );
        activity.isActive( input.getIsActive() );
        activity.tenantId( input.getTenantId() );

        return activity.build();
    }

    @Override
    public Activity toDomain(ActivityUpdateInput input) {
        if ( input == null ) {
            return null;
        }

        Activity.ActivityBuilder activity = Activity.builder();

        activity.id( input.getId() );
        activity.name( input.getName() );
        activity.description( input.getDescription() );
        activity.isActive( input.getIsActive() );
        activity.tenantId( input.getTenantId() );

        return activity.build();
    }

    @Override
    public ActivityResponse toResponse(Activity activity) {
        if ( activity == null ) {
            return null;
        }

        ActivityResponse activityResponse = new ActivityResponse();

        activityResponse.setId( activity.getId() );
        activityResponse.setName( activity.getName() );
        activityResponse.setDescription( activity.getDescription() );
        activityResponse.setIsActive( activity.getIsActive() );
        activityResponse.setTenantId( activity.getTenantId() );

        return activityResponse;
    }

    @Override
    public List<ActivityResponse> toResponseList(List<Activity> activities) {
        if ( activities == null ) {
            return null;
        }

        List<ActivityResponse> list = new ArrayList<ActivityResponse>( activities.size() );
        for ( Activity activity : activities ) {
            list.add( toResponse( activity ) );
        }

        return list;
    }

    @Override
    public ActivityPageResponse toPageResponse(PageResponse<Activity> pageResponse) {
        if ( pageResponse == null ) {
            return null;
        }

        ActivityPageResponse activityPageResponse = new ActivityPageResponse();

        activityPageResponse.setContent( toResponseList( pageResponse.getContent() ) );
        activityPageResponse.setPage( pageResponse.getPage() );
        activityPageResponse.setSize( pageResponse.getSize() );
        activityPageResponse.setTotalElements( pageResponse.getTotalElements() );
        activityPageResponse.setTotalPages( pageResponse.getTotalPages() );

        return activityPageResponse;
    }
}
