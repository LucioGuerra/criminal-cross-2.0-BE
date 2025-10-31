package org.athlium.gym.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Organization {

    private Long id;
    private String name; // nombre de la organización
    private List<Headquarters> headQuarters; // Lista de sedes de la organización
    // TODO: Manejamos el tier a nivel organización y despues unos addons por sede (mp, estadisticas, entre otras cosas)
//    private Tier tier; // nivel de servicio contratado
//    private TierState tierState; // estado de la suscripción o pago del nivel de servicio
//    private LocalDateTime tierExpiration; // fecha de expiración del nivel de servicio (para acceso mas rapido)
//    private LocalDateTime tierStartDate; // fecha de inicio del nivel de servicio (para acceso mas rapido)
//    private List<Long> owners; // Lista de usuarios que son dueños de la organización
//
//    // Datos legales y fiscales de la organizacion
    // TODO: Si el tier lo manejamos por sede, estos datos van a nivel sede
//    private String legalName; // razón social
//    private String taxId; // CUIT, NIT, RFC, etc.
//    private String address; // dirección fiscal
//    private String city; // ciudad fiscal
//    private String province; // provincia o estado fiscal
//    private String postalCode; // código postal fiscal
//    private String country; // país fiscal
//    private String contactEmail; // email de contacto administrativo
//    private String contactPhone; // teléfono de contacto administrativo
//
//    // Datos de facturación (pagos de clientes a la organización)
//    private String billingEmail; // email para envío de facturas
//    private String cbu; // CBU, cuenta bancaria
//    private String currency; // moneda de facturación
//    // ver que mas necesitamos acá
//
//    // Datos de pago (pagos de la organización al servicio)
//    private String paymentMethod; // métod de pago (tarjeta de crédito, débito automático, etc.) proximamente sera una enum
//    private String paymentAccount; // cuenta de pago (número de tarjeta, cuenta bancaria, etc.)
//    private Object payments; // historial de pagos del nivel de servicio (puede ser una lista de objetos de pago) capaz aca podemos incluir el tier con sus detalles
//    // proximamente podriamos hacer una clase Payment para que puedan tener varias opciones y elegir en cada momento cual usar y definir el preferido

}
