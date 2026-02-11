package org.athlium.bookings.infrastructure.mapper;

import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.infrastructure.entity.BookingEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta-cdi")
public interface BookingMapper {

    Booking toDomain(BookingEntity entity);

    BookingEntity toEntity(Booking booking);
}
