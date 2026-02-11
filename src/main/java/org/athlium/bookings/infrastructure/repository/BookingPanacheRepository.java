package org.athlium.bookings.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.athlium.bookings.infrastructure.entity.BookingEntity;

@ApplicationScoped
public class BookingPanacheRepository implements PanacheRepository<BookingEntity> {
}
