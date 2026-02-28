package org.athlium.payments.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.payments.infrastructure.entity.PaymentEntity;

@ApplicationScoped
public class PaymentPanacheRepository implements PanacheRepository<PaymentEntity> {
}
