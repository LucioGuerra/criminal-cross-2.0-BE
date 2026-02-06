package org.athlium.gym.infrastructure.document;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;

@MongoEntity(collection = "gym_config")
public class OrganizationConfigDocument extends PanacheMongoEntity {
    public Long organizationId;
    public SessionConfigurationDocument configuration;
}
