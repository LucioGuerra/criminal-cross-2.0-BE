package org.athlium.gym.domain.model;

public enum Tier {
    STANDARD, // pago básico, menos características (1 sede, cantidad limitada de usuario activos?, funcionalidades limitadas?, mercado pago no disponible?, balances no disponibles?)
    PREMIUM, // pago completo, más características (hasta 3 o 5 sedes?, usuarios ilimitados?, funcionalidades completas?, conexion con mercado pago para cobros? balances disponibles?)
    ENTERPRISE, // pago empresarial, todas las características (sedes ilimitadas, usuarios ilimitados, funcionalidades exclusivas?, conexion con whatsapp para campañas publicitarias?)
}
// Ver si nos conviene tener por un lado un enum de estados de suscripción y por otro lado un enum de niveles de servicio (tiers)