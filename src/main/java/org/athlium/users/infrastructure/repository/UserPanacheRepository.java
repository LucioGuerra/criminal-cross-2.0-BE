package org.athlium.users.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.users.infrastructure.entity.UserEntity;

@ApplicationScoped
public class UserPanacheRepository implements PanacheRepository<UserEntity> {
}
