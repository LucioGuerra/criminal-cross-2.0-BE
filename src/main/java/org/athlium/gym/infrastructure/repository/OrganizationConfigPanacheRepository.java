package org.athlium.gym.infrastructure.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.gym.infrastructure.document.OrganizationConfigDocument;
import org.bson.types.ObjectId;

@ApplicationScoped
public class OrganizationConfigPanacheRepository implements PanacheMongoRepositoryBase<OrganizationConfigDocument, ObjectId> {
}
