package org.athlium.gym.domain.model;

public enum TierState {
    ACTIVE, // estado normal cuando la organización está al día con los pagos y suscripciones
    WAITING_FOR_PAYMENT, // estado temporal mientras se procesa el pago o la suscripción
    NO_PAYMENT, // estado cuando no se ha realizado el pago o la suscripción ha expirado
    SUSPENDED, // estado cuando la cuenta ha sido suspendida violación de términos
    CANCELLED, // estado cuando la cuenta ha sido cancelada por el usuario (nos va a permitir ofrecer reactivación fácil?)
    TESTING, // estado temporal para organizaciones en período de prueba que podria ser de 1 o 2 semanas?
}
