package org.athlium.clients.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.clients.infrastructure.entity.ClientPackageEntity;

@ApplicationScoped
public class ClientPackagePanacheRepository implements PanacheRepository<ClientPackageEntity> {
}
