package org.athlium.clients.infrastructure.repository;

import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.infrastructure.entity.ClientPackageEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientPackageRepositoryImplTest {

    @Test
    void shouldUpdateManagedEntityWithoutCallingPersist() {
        var repository = new ClientPackageRepositoryImpl();
        var panache = new FakeClientPackagePanacheRepository();
        repository.clientPackagePanacheRepository = panache;

        var managed = new ClientPackageEntity();
        managed.id = 100L;
        managed.userId = 1L;
        managed.paymentId = 10L;
        managed.periodStart = LocalDate.of(2026, 1, 1);
        managed.periodEnd = LocalDate.of(2026, 1, 31);
        managed.active = true;
        panache.stored = managed;

        var update = new ClientPackage();
        update.setId(100L);
        update.setUserId(2L);
        update.setPaymentId(20L);
        update.setPeriodStart(LocalDate.of(2026, 2, 1));
        update.setPeriodEnd(LocalDate.of(2026, 2, 28));
        update.setActive(false);

        var credit = new ClientPackageCredit();
        credit.setActivityId(9L);
        credit.setTokens(12);
        update.setCredits(List.of(credit));

        repository.save(update);

        assertEquals(0, panache.persistCalls);
        assertEquals(2L, managed.userId);
        assertEquals(20L, managed.paymentId);
        assertEquals(LocalDate.of(2026, 2, 1), managed.periodStart);
        assertEquals(LocalDate.of(2026, 2, 28), managed.periodEnd);
        assertEquals(false, managed.active);
        assertEquals(1, managed.credits.size());
        assertEquals(9L, managed.credits.getFirst().activityId);
        assertEquals(12, managed.credits.getFirst().tokens);
    }

    private static final class FakeClientPackagePanacheRepository extends ClientPackagePanacheRepository {
        ClientPackageEntity stored;
        int persistCalls;

        @Override
        public ClientPackageEntity findById(Long id) {
            return stored != null && stored.id.equals(id) ? stored : null;
        }

        @Override
        public void persist(ClientPackageEntity entity) {
            persistCalls++;
            stored = entity;
        }
    }
}
