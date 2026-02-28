package org.athlium.gym.infrastructure.repository;

import org.athlium.gym.domain.model.Activity;
import org.athlium.gym.domain.model.Headquarters;
import org.athlium.gym.domain.model.SessionInstance;
import org.athlium.gym.domain.model.SessionSource;
import org.athlium.gym.domain.model.SessionStatus;
import org.athlium.gym.domain.model.WaitlistStrategy;
import org.athlium.gym.infrastructure.entity.ActivityEntity;
import org.athlium.gym.infrastructure.entity.HeadquartersEntity;
import org.athlium.gym.infrastructure.entity.SessionInstanceEntity;
import org.athlium.gym.infrastructure.mapper.ActivityMapper;
import org.athlium.gym.infrastructure.mapper.HeadquartersMapper;
import org.athlium.gym.infrastructure.mapper.SessionInstanceMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryUpdatePersistencePatternTest {

    @Test
    void shouldUpdateManagedHeadquartersWithoutCallingPersist() {
        var repository = new HeadquartersRepositoryImpl();
        var panache = new FakeHeadquartersPanacheRepository();
        var mapper = new FakeHeadquartersMapper();
        repository.panacheRepository = panache;
        repository.mapper = mapper;

        var managed = new HeadquartersEntity();
        managed.id = 10L;
        managed.setOrganizationId(1L);
        managed.setName("HQ original");
        panache.stored = managed;

        var update = Headquarters.builder()
                .id(10L)
                .organizationId(2L)
                .name("HQ actualizada")
                .build();

        repository.save(update);

        assertEquals(0, panache.persistCalls);
        assertEquals(2L, managed.getOrganizationId());
        assertEquals("HQ actualizada", managed.getName());
    }

    @Test
    void shouldUpdateManagedSessionInstanceWithoutCallingPersist() {
        var repository = new SessionInstanceRepositoryImpl();
        var panache = new FakeSessionInstancePanacheRepository();
        var mapper = new FakeSessionInstanceMapper();
        repository.panacheRepository = panache;
        repository.mapper = mapper;

        var managed = new SessionInstanceEntity();
        managed.id = 30L;
        managed.setOrganizationId(1L);
        managed.setHeadquartersId(1L);
        managed.setActivityId(1L);
        managed.setStartsAt(Instant.parse("2026-02-27T10:00:00Z"));
        managed.setEndsAt(Instant.parse("2026-02-27T11:00:00Z"));
        managed.setStatus(SessionStatus.OPEN);
        managed.setSource(SessionSource.MANUAL);
        managed.setMaxParticipants(10);
        managed.setWaitlistEnabled(true);
        managed.setWaitlistMaxSize(5);
        managed.setWaitlistStrategy(WaitlistStrategy.FIFO);
        managed.setCancellationMinHoursBeforeStart(6);
        managed.setCancellationAllowLateCancel(false);
        panache.stored = managed;

        var update = new SessionInstance();
        update.setId(30L);
        update.setOrganizationId(2L);
        update.setHeadquartersId(3L);
        update.setActivityId(4L);
        update.setStartsAt(Instant.parse("2026-02-27T12:00:00Z"));
        update.setEndsAt(Instant.parse("2026-02-27T13:00:00Z"));
        update.setStatus(SessionStatus.CANCELLED);
        update.setSource(SessionSource.SCHEDULER);
        update.setMaxParticipants(20);
        update.setWaitlistEnabled(false);
        update.setWaitlistMaxSize(0);
        update.setWaitlistStrategy(WaitlistStrategy.FIFO);
        update.setCancellationMinHoursBeforeStart(2);
        update.setCancellationAllowLateCancel(true);

        repository.save(update);

        assertEquals(0, panache.persistCalls);
        assertEquals(2L, managed.getOrganizationId());
        assertEquals(3L, managed.getHeadquartersId());
        assertEquals(4L, managed.getActivityId());
        assertEquals(Instant.parse("2026-02-27T12:00:00Z"), managed.getStartsAt());
        assertEquals(Instant.parse("2026-02-27T13:00:00Z"), managed.getEndsAt());
        assertEquals(SessionStatus.CANCELLED, managed.getStatus());
        assertEquals(SessionSource.SCHEDULER, managed.getSource());
        assertEquals(20, managed.getMaxParticipants());
        assertEquals(false, managed.getWaitlistEnabled());
        assertEquals(0, managed.getWaitlistMaxSize());
        assertEquals(WaitlistStrategy.FIFO, managed.getWaitlistStrategy());
        assertEquals(2, managed.getCancellationMinHoursBeforeStart());
        assertEquals(true, managed.getCancellationAllowLateCancel());
    }

    @Test
    void shouldUpdateManagedActivityWithoutCallingPersist() {
        var repository = new ActivityRepositoryImpl();
        var panache = new FakeActivityPanacheRepository();
        var mapper = new FakeActivityMapper();
        repository.panacheRepo = panache;
        repository.mapper = mapper;

        var managed = new ActivityEntity();
        managed.id = 40L;
        managed.setName("Funcional");
        managed.setDescription("Desc vieja");
        managed.setIsActive(true);
        managed.setHqId(1L);
        panache.stored = managed;

        var update = Activity.builder()
                .id(40L)
                .name("Pilates")
                .description("Desc nueva")
                .isActive(false)
                .hqId(2L)
                .build();

        repository.update(update);

        assertEquals(0, panache.persistCalls);
        assertEquals("Pilates", managed.getName());
        assertEquals("Desc nueva", managed.getDescription());
        assertEquals(false, managed.getIsActive());
        assertEquals(2L, managed.getHqId());
    }

    private static final class FakeHeadquartersPanacheRepository extends HeadquartersPanacheRepository {
        HeadquartersEntity stored;
        int persistCalls;

        @Override
        public HeadquartersEntity findById(Long id) {
            return stored != null && stored.id.equals(id) ? stored : null;
        }

        @Override
        public void persist(HeadquartersEntity entity) {
            persistCalls++;
            stored = entity;
        }

        @Override
        public List<HeadquartersEntity> findByOrganizationId(Long organizationId) {
            return List.of();
        }
    }

    private static final class FakeSessionInstancePanacheRepository extends SessionInstancePanacheRepository {
        SessionInstanceEntity stored;
        int persistCalls;

        @Override
        public SessionInstanceEntity findById(Long id) {
            return stored != null && stored.id.equals(id) ? stored : null;
        }

        @Override
        public void persist(SessionInstanceEntity entity) {
            persistCalls++;
            stored = entity;
        }
    }

    private static final class FakeActivityPanacheRepository extends ActivityPanacheRepository {
        ActivityEntity stored;
        int persistCalls;

        @Override
        public ActivityEntity findById(Long id) {
            return stored != null && stored.id.equals(id) ? stored : null;
        }

        @Override
        public void persist(ActivityEntity entity) {
            persistCalls++;
            stored = entity;
        }
    }

    private static final class FakeHeadquartersMapper implements HeadquartersMapper {

        @Override
        public Headquarters toDomain(HeadquartersEntity entity) {
            return Headquarters.builder()
                    .id(entity.id)
                    .organizationId(entity.getOrganizationId())
                    .name(entity.getName())
                    .build();
        }

        @Override
        public HeadquartersEntity toEntity(Headquarters domain) {
            var entity = new HeadquartersEntity();
            entity.id = domain.getId();
            entity.setOrganizationId(domain.getOrganizationId());
            entity.setName(domain.getName());
            return entity;
        }
    }

    private static final class FakeSessionInstanceMapper implements SessionInstanceMapper {

        @Override
        public SessionInstance toDomain(SessionInstanceEntity entity) {
            var session = new SessionInstance();
            session.setId(entity.id);
            session.setOrganizationId(entity.getOrganizationId());
            session.setHeadquartersId(entity.getHeadquartersId());
            session.setActivityId(entity.getActivityId());
            session.setStartsAt(entity.getStartsAt());
            session.setEndsAt(entity.getEndsAt());
            session.setStatus(entity.getStatus());
            session.setSource(entity.getSource());
            session.setMaxParticipants(entity.getMaxParticipants());
            session.setWaitlistEnabled(entity.getWaitlistEnabled());
            session.setWaitlistMaxSize(entity.getWaitlistMaxSize());
            session.setWaitlistStrategy(entity.getWaitlistStrategy());
            session.setCancellationMinHoursBeforeStart(entity.getCancellationMinHoursBeforeStart());
            session.setCancellationAllowLateCancel(entity.getCancellationAllowLateCancel());
            return session;
        }

        @Override
        public SessionInstanceEntity toEntity(SessionInstance domain) {
            var entity = new SessionInstanceEntity();
            entity.id = domain.getId();
            entity.setOrganizationId(domain.getOrganizationId());
            entity.setHeadquartersId(domain.getHeadquartersId());
            entity.setActivityId(domain.getActivityId());
            entity.setStartsAt(domain.getStartsAt());
            entity.setEndsAt(domain.getEndsAt());
            entity.setStatus(domain.getStatus());
            entity.setSource(domain.getSource());
            entity.setMaxParticipants(domain.getMaxParticipants());
            entity.setWaitlistEnabled(domain.getWaitlistEnabled());
            entity.setWaitlistMaxSize(domain.getWaitlistMaxSize());
            entity.setWaitlistStrategy(domain.getWaitlistStrategy());
            entity.setCancellationMinHoursBeforeStart(domain.getCancellationMinHoursBeforeStart());
            entity.setCancellationAllowLateCancel(domain.getCancellationAllowLateCancel());
            return entity;
        }
    }

    private static final class FakeActivityMapper implements ActivityMapper {

        @Override
        public Activity toDomain(ActivityEntity entity) {
            return Activity.builder()
                    .id(entity.id)
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .isActive(entity.getIsActive())
                    .hqId(entity.getHqId())
                    .build();
        }

        @Override
        public ActivityEntity toEntity(Activity domain) {
            var entity = new ActivityEntity();
            entity.id = domain.getId();
            entity.setName(domain.getName());
            entity.setDescription(domain.getDescription());
            entity.setIsActive(domain.getIsActive());
            entity.setHqId(domain.getHqId());
            return entity;
        }

        @Override
        public void updateEntityFromDomain(Activity domain, ActivityEntity entity) {
            entity.setName(domain.getName());
            entity.setDescription(domain.getDescription());
            entity.setIsActive(domain.getIsActive());
            entity.setHqId(domain.getHqId());
        }
    }
}
