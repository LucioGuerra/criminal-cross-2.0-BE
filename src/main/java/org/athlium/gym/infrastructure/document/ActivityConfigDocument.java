package org.athlium.gym.infrastructure.document;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "activity_config")
public class ActivityConfigDocument extends PanacheMongoEntity {
    public Long activityId;
    public SessionConfigurationDocument configuration;
}
