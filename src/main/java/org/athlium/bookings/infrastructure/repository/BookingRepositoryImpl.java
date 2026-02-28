package org.athlium.bookings.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import org.athlium.bookings.domain.model.Booking;
import org.athlium.bookings.domain.model.BookingStatus;
import org.athlium.bookings.domain.repository.BookingRepository;
import org.athlium.bookings.infrastructure.entity.BookingEntity;
import org.athlium.bookings.infrastructure.mapper.BookingMapper;
import org.athlium.shared.domain.PageResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookingRepositoryImpl implements BookingRepository {

    @Inject
    BookingPanacheRepository bookingPanacheRepository;

    @Inject
    BookingMapper bookingMapper;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity;
        if (booking.getId() != null) {
            entity = bookingPanacheRepository.findById(booking.getId());
            if (entity == null) {
                entity = bookingMapper.toEntity(booking);
                entity.id = booking.getId();
                bookingPanacheRepository.persist(entity);
                return bookingMapper.toDomain(entity);
            }
            entity.sessionId = booking.getSessionId();
            entity.userId = booking.getUserId();
            entity.status = booking.getStatus();
            entity.cancelledAt = booking.getCancelledAt();
            entity.createRequestId = booking.getCreateRequestId();
            entity.cancelRequestId = booking.getCancelRequestId();
            entity.promotedBookingId = booking.getPromotedBookingId();
            entity.consumedPackageId = booking.getConsumedPackageId();
            return bookingMapper.toDomain(entity);
        }

        entity = bookingMapper.toEntity(booking);
        bookingPanacheRepository.persist(entity);
        return bookingMapper.toDomain(entity);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingPanacheRepository.findByIdOptional(id).map(bookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findByIdForUpdate(Long id) {
        BookingEntity entity = bookingPanacheRepository.findById(id, LockModeType.PESSIMISTIC_WRITE);
        return Optional.ofNullable(entity).map(bookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findByCreateRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return Optional.empty();
        }
        BookingEntity entity = bookingPanacheRepository.find("createRequestId", requestId).firstResult();
        return Optional.ofNullable(entity).map(bookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findByCancelRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return Optional.empty();
        }
        BookingEntity entity = bookingPanacheRepository.find("cancelRequestId", requestId).firstResult();
        return Optional.ofNullable(entity).map(bookingMapper::toDomain);
    }

    @Override
    public boolean existsActiveBooking(Long sessionId, Long userId) {
        return bookingPanacheRepository.count(
                "sessionId = ?1 and userId = ?2 and status in (?3, ?4)",
                sessionId,
                userId,
                BookingStatus.CONFIRMED,
                BookingStatus.WAITLISTED
        ) > 0;
    }

    @Override
    public long countBySessionAndStatus(Long sessionId, BookingStatus status) {
        return bookingPanacheRepository.count("sessionId = ?1 and status = ?2", sessionId, status);
    }

    @Override
    public Optional<Booking> findFirstWaitlistedBySessionId(Long sessionId) {
        BookingEntity entity = bookingPanacheRepository.find(
                        "sessionId = ?1 and status = ?2 order by createdAt asc",
                        sessionId,
                        BookingStatus.WAITLISTED
                )
                .firstResult();
        return Optional.ofNullable(entity).map(bookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findFirstWaitlistedBySessionIdForUpdate(Long sessionId) {
        BookingEntity entity = bookingPanacheRepository.find(
                        "sessionId = ?1 and status = ?2 order by createdAt asc",
                        sessionId,
                        BookingStatus.WAITLISTED
                )
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
        return Optional.ofNullable(entity).map(bookingMapper::toDomain);
    }

    @Override
    public PageResponse<Booking> findBookings(
            Long sessionId,
            Long userId,
            BookingStatus status,
            Long branchId,
            Long activityId,
            Instant from,
            Instant to,
            int page,
            int size,
            boolean sortAscending
    ) {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();

        appendCondition(queryBuilder, params, "sessionId =", sessionId);
        appendCondition(queryBuilder, params, "userId =", userId);
        appendCondition(queryBuilder, params, "status =", status);

        if (branchId != null || activityId != null || from != null || to != null) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" and ");
            }

            StringBuilder subQuery = new StringBuilder("sessionId in (select s.id from SessionInstanceEntity s where 1=1");
            if (branchId != null) {
                subQuery.append(" and s.headquartersId = ?").append(params.size() + 1);
                params.add(branchId);
            }
            if (activityId != null) {
                subQuery.append(" and s.activityId = ?").append(params.size() + 1);
                params.add(activityId);
            }
            if (from != null) {
                subQuery.append(" and s.startsAt >= ?").append(params.size() + 1);
                params.add(from);
            }
            if (to != null) {
                subQuery.append(" and s.startsAt <= ?").append(params.size() + 1);
                params.add(to);
            }
            subQuery.append(")");

            queryBuilder.append(subQuery);
        }

        String orderBy = " order by createdAt " + (sortAscending ? "asc" : "desc");

        PanacheQuery<BookingEntity> query;
        if (params.isEmpty()) {
            query = bookingPanacheRepository.find("from BookingEntity" + orderBy);
        } else {
            query = bookingPanacheRepository.find(queryBuilder + orderBy, params.toArray());
        }

        query.page(Page.of(page, size));

        List<Booking> bookings = query.list().stream().map(bookingMapper::toDomain).toList();
        return new PageResponse<>(bookings, page, size, query.count());
    }

    private void appendCondition(StringBuilder queryBuilder, List<Object> params, String expression, Object value) {
        if (value == null) {
            return;
        }
        if (queryBuilder.length() > 0) {
            queryBuilder.append(" and ");
        }
        queryBuilder.append(expression).append(" ?").append(params.size() + 1);
        params.add(value);
    }
}
