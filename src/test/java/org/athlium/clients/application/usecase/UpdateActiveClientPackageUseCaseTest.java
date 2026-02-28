package org.athlium.clients.application.usecase;

import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateActiveClientPackageUseCaseTest {

    private UpdateActiveClientPackageUseCase useCase;
    private InMemoryClientPackageRepository repository;

    @BeforeEach
    void setUp() {
        useCase = new UpdateActiveClientPackageUseCase();
        repository = new InMemoryClientPackageRepository();
        useCase.clientPackageRepository = repository;
    }

    @Test
    void shouldUpdateActivePackageCredits() {
        ClientPackage active = new ClientPackage();
        active.setId(1L);
        active.setUserId(20L);
        active.setActive(true);
        active.setPeriodEnd(LocalDate.now().plusDays(2));
        active.setCredits(new ArrayList<>(List.of(
                credit(100L, 5),
                credit(200L, 8)
        )));
        repository.saved.add(active);

        ClientPackage updated = useCase.execute(20L, 1L, List.of(
                credit(200L, 12),
                credit(300L, 4)
        ));

        assertEquals(1L, updated.getId());
        assertEquals(3, updated.getCredits().size());
        assertEquals(100L, updated.getCredits().get(0).getActivityId());
        assertEquals(5, updated.getCredits().get(0).getTokens());
        assertEquals(200L, updated.getCredits().get(1).getActivityId());
        assertEquals(12, updated.getCredits().get(1).getTokens());
        assertEquals(300L, updated.getCredits().get(2).getActivityId());
        assertEquals(4, updated.getCredits().get(2).getTokens());
    }

    @Test
    void shouldFailWhenNoActivePackageExists() {
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                useCase.execute(20L, 999L, credits(200L, 12))
        );

        assertEquals("Client package with id 999 not found", ex.getMessage());
    }

    @Test
    void shouldFailWhenPackageDoesNotBelongToUser() {
        ClientPackage active = new ClientPackage();
        active.setId(1L);
        active.setUserId(99L);
        active.setActive(true);
        active.setPeriodEnd(LocalDate.now().plusDays(2));
        repository.saved.add(active);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                useCase.execute(20L, 1L, credits(200L, 12))
        );

        assertEquals("Package does not belong to the user", ex.getMessage());
    }

    private List<ClientPackageCredit> credits(Long activityId, Integer tokens) {
        return List.of(credit(activityId, tokens));
    }

    private ClientPackageCredit credit(Long activityId, Integer tokens) {
        ClientPackageCredit credit = new ClientPackageCredit();
        credit.setActivityId(activityId);
        credit.setTokens(tokens);
        return credit;
    }

    private static class InMemoryClientPackageRepository implements ClientPackageRepository {
        private final List<ClientPackage> saved = new ArrayList<>();

        @Override
        public ClientPackage save(ClientPackage clientPackage) {
            int index = -1;
            for (int i = 0; i < saved.size(); i++) {
                if (saved.get(i).getId().equals(clientPackage.getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                saved.set(index, clientPackage);
            } else {
                saved.add(clientPackage);
            }
            return clientPackage;
        }

        @Override
        public List<ClientPackage> findActiveByUserId(Long userId) {
            return saved.stream()
                    .filter(p -> userId.equals(p.getUserId()) && Boolean.TRUE.equals(p.getActive()))
                    .toList();
        }

        @Override
        public List<ClientPackage> findActiveByUserIdForUpdate(Long userId) {
            return findActiveByUserId(userId);
        }

        @Override
        public Optional<ClientPackage> findByIdForUpdate(Long packageId) {
            return saved.stream().filter(p -> packageId.equals(p.getId())).findFirst();
        }

        @Override
        public List<ClientPackage> findByUserId(Long userId) {
            return saved;
        }

        @Override
        public long deactivateExpiredPackagesByUserId(Long userId, LocalDate today) {
            saved.stream()
                    .filter(p -> userId.equals(p.getUserId()))
                    .filter(p -> p.getPeriodEnd() != null && today.isAfter(p.getPeriodEnd()))
                    .forEach(p -> p.setActive(false));
            return 0;
        }

        @Override
        public long deactivateExpiredPackages(LocalDate today) {
            saved.stream()
                    .filter(p -> p.getPeriodEnd() != null && today.isAfter(p.getPeriodEnd()))
                    .forEach(p -> p.setActive(false));
            return 0;
        }
    }
}
