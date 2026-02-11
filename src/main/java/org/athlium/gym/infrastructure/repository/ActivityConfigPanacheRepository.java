package org.athlium.gym.infrastructure.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.document.ActivityConfigDocument;
import org.bson.types.ObjectId;

@ApplicationScoped
public class ActivityConfigPanacheRepository implements PanacheMongoRepositoryBase<ActivityConfigDocument, ObjectId> {
}
