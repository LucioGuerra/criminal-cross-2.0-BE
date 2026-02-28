package org.athlium.gym.infrastructure.document;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "branch_config")
public class HeadquartersConfigDocument extends PanacheMongoEntity {
    public Long headquartersId;
    public SessionConfigurationDocument configuration;
}
