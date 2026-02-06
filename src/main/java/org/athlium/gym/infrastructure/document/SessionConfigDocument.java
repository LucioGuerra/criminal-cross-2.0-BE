package org.athlium.gym.infrastructure.document;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "session_overrides")
public class SessionConfigDocument extends PanacheMongoEntity {
    public Long sessionId;
    public SessionConfigurationDocument configuration;
}
