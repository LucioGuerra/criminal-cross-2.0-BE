package org.athlium.payments.domain.repository;

import org.athlium.payments.domain.model.Payment;
import org.athlium.payments.domain.model.PaymentListItem;
import org.athlium.payments.domain.model.PaymentSearchCriteria;
import org.athlium.shared.domain.PageResponse;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    PageResponse<PaymentListItem> findPayments(PaymentSearchCriteria criteria);
}
