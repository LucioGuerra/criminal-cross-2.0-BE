package org.athlium.payments.domain.repository;

import org.athlium.payments.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);
}
