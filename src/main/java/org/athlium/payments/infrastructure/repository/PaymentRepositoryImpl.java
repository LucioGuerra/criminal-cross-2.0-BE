package org.athlium.payments.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.repository.PaymentRepository;
import org.athlium.payments.infrastructure.entity.PaymentEntity;

import java.util.Optional;

@ApplicationScoped
public class PaymentRepositoryImpl implements PaymentRepository {

    @Inject
    PaymentPanacheRepository paymentPanacheRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.amount = payment.getAmount();
        entity.method = payment.getMethod();
        entity.paidAt = payment.getPaidAt();
        paymentPanacheRepository.persist(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentPanacheRepository.findByIdOptional(paymentId).map(this::toDomain);
    }

    private Payment toDomain(PaymentEntity entity) {
        Payment payment = new Payment();
        payment.setId(entity.id);
        payment.setAmount(entity.amount);
        payment.setMethod(entity.method);
        payment.setPaidAt(entity.paidAt);
        payment.setCreatedAt(entity.createdAt);
        payment.setUpdatedAt(entity.updatedAt);
        return payment;
    }
}
