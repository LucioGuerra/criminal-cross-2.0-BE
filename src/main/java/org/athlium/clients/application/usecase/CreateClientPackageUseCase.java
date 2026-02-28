package org.athlium.clients.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.athlium.clients.domain.model.ClientPackage;
import org.athlium.clients.domain.model.ClientPackageCredit;
import org.athlium.clients.domain.repository.ClientPackageRepository;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.shared.exception.BadRequestException;
import org.athlium.shared.exception.EntityNotFoundException;
import org.athlium.users.domain.repository.UserRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class CreateClientPackageUseCase {

    @Inject
    ClientPackageRepository clientPackageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Transactional
    public ClientPackage execute(Long userId, Long paymentId, List<ClientPackageCredit> credits) {
        validateUserId(userId);
        validatePaymentId(paymentId);
        validateCredits(credits);
        ensureUserExists(userId);
        ensurePaymentExists(paymentId);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clientPackageRepository.deactivateExpiredPackagesByUserId(userId, today);

        ClientPackage clientPackage = new ClientPackage();
        clientPackage.setUserId(userId);
        clientPackage.setPaymentId(paymentId);
        clientPackage.setPeriodStart(today);
        clientPackage.setPeriodEnd(today.plusMonths(1));
        clientPackage.setActive(true);
        clientPackage.setCredits(credits);
        return clientPackageRepository.save(clientPackage);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId must be a positive number");
        }
    }

    private void ensureUserExists(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    private void validatePaymentId(Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            throw new BadRequestException("paymentId must be a positive number");
        }
    }

    private void ensurePaymentExists(Long paymentId) {
        paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));
    }

    private void validateCredits(List<ClientPackageCredit> credits) {
        if (credits == null || credits.isEmpty()) {
            throw new BadRequestException("At least one activity credit is required");
        }
        for (ClientPackageCredit credit : credits) {
            if (credit.getActivityId() == null || credit.getActivityId() <= 0) {
                throw new BadRequestException("activityId must be a positive number");
            }
            if (credit.getTokens() == null || credit.getTokens() <= 0) {
                throw new BadRequestException("tokens must be greater than 0");
            }
        }
    }
}
