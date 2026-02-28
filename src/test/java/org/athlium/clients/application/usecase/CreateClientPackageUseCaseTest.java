package org.athlium.clients.application.usecase;

import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.users.domain.model.User;
import org.athlium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreateClientPackageUseCaseTest {

    private CreateClientPackageUseCase useCase;
    private InMemoryClientPackageRepository clientPackageRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryPaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        useCase = new CreateClientPackageUseCase();
        clientPackageRepository = new InMemoryClientPackageRepository();
        userRepository = new InMemoryUserRepository();
        paymentRepository = new InMemoryPaymentRepository();

        useCase.clientPackageRepository = clientPackageRepository;
        useCase.userRepository = userRepository;
        useCase.paymentRepository = paymentRepository;

        User user = User.builder().id(10L).name("Client").lastName("A").email("c@a.com").firebaseUid("uid").active(true).build();
        userRepository.users.add(user);

        Payment payment = new Payment();
        payment.setId(1L);
        paymentRepository.payments.add(payment);
    }

    @Test
    void shouldCreatePackageWhenUserHasNoActivePackage() {
        LocalDate today = LocalDate.now();
        ClientPackage created = useCase.execute(10L, 1L, credits(101L, 8));

        assertNotNull(created);
        assertEquals(10L, created.getUserId());
        assertEquals(1L, created.getPaymentId());
        assertEquals(true, created.getActive());
        assertEquals(today, created.getPeriodStart());
        assertEquals(today.plusMonths(1), created.getPeriodEnd());
        assertEquals(1, created.getCredits().size());
        assertEquals(1, clientPackageRepository.saved.size());
    }

    @Test
    void shouldCreateAnotherActivePackageForSameUser() {
        ClientPackage active = new ClientPackage();
        active.setId(1L);
        active.setUserId(10L);
        active.setActive(true);
        active.setPeriodEnd(LocalDate.now().plusDays(2));
        clientPackageRepository.saved.add(active);

        ClientPackage created = useCase.execute(10L, 1L, credits(101L, 8));

        assertNotNull(created);
        assertEquals(2, clientPackageRepository.saved.size());
        assertEquals(true, created.getActive());
    }

    private List<ClientPackageCredit> credits(Long activityId, Integer tokens) {
        ClientPackageCredit credit = new ClientPackageCredit();
        credit.setActivityId(activityId);
        credit.setTokens(tokens);
        return List.of(credit);
    }

    private static class InMemoryClientPackageRepository implements ClientPackageRepository {
        private final List<ClientPackage> saved = new ArrayList<>();

        @Override
        public ClientPackage save(ClientPackage clientPackage) {
            if (clientPackage.getId() == null) {
                clientPackage.setId((long) (saved.size() + 1));
            }
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

    private static class InMemoryUserRepository implements UserRepository {
        private final List<User> users = new ArrayList<>();

        @Override
        public Optional<User> findByFirebaseUid(String firebaseUid) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public User save(User user) {
            users.add(user);
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return users.stream().filter(user -> id.equals(user.getId())).findFirst();
        }

        @Override
        public void deleteById(Long id) {
        }
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        private final List<Payment> payments = new ArrayList<>();

        @Override
        public Payment save(Payment payment) {
            payments.add(payment);
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long paymentId) {
            return payments.stream().filter(payment -> paymentId.equals(payment.getId())).findFirst();
        }
    }
}
