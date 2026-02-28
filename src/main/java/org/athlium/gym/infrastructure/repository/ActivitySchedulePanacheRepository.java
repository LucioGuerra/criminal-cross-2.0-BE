package org.athlium.gym.infrastructure.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.document.ActivityScheduleDocument;
import org.bson.types.ObjectId;

@ApplicationScoped
public class ActivitySchedulePanacheRepository implements PanacheMongoRepositoryBase<ActivityScheduleDocument, ObjectId> {
}
