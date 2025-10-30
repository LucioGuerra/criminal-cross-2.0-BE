package org.athlium.gym.infrastructure.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import javax.annotation.processing.Generated;
import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.infrastructure.entity.ActivityEntity;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-30T00:28:14-0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.8 (JetBrains s.r.o.)"
)
@ApplicationScoped
public class ActivityMapperImpl implements ActivityMapper {

    @Override
    public Activity toDomain(ActivityEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Activity.ActivityBuilder activity = Activity.builder();

        activity.id( entity.getId() );
        activity.name( entity.getName() );
        activity.description( entity.getDescription() );
        activity.isActive( entity.getIsActive() );
        activity.tenantId( entity.getTenantId() );

        return activity.build();
    }

    @Override
    public ActivityEntity toEntity(Activity domain) {
        if ( domain == null ) {
            return null;
        }

        ActivityEntity activityEntity = new ActivityEntity();

        activityEntity.setId( domain.getId() );
        activityEntity.setName( domain.getName() );
        activityEntity.setDescription( domain.getDescription() );
        activityEntity.setIsActive( domain.getIsActive() );
        activityEntity.setTenantId( domain.getTenantId() );

        return activityEntity;
    }

    @Override
    public void updateEntityFromDomain(Activity domain, ActivityEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getId() != null ) {
            entity.setId( domain.getId() );
        }
        if ( domain.getName() != null ) {
            entity.setName( domain.getName() );
        }
        if ( domain.getDescription() != null ) {
            entity.setDescription( domain.getDescription() );
        }
        if ( domain.getIsActive() != null ) {
            entity.setIsActive( domain.getIsActive() );
        }
        if ( domain.getTenantId() != null ) {
            entity.setTenantId( domain.getTenantId() );
        }
    }
}
