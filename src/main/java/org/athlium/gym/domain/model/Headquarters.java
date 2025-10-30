package org.athlium.gym.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Headquarters {

    private Long id;
    private String organizationId; // ID de la organización a la que pertenece la sede
    private String name; // nombre completo de la sede
//    private String alias; // nombre corto o apodo de la sede
//    private String address; // dirección física de la sede
//    private String city; // ciudad donde se encuentra la sede
//    private String province; // provincia o estado donde se encuentra la sede
//    private String postalCode; // código postal de la sede
//    private String country; // país donde se encuentra la sede
//
//    private String phone;
//    private String email;
//    private String openHours; // horario de apertura y cierre, no se si mejor lo saco? igual el tipo de dato no seria String
//    private boolean active;
//
    private List<Activity> activities; // Lista de actividades disponibles en la sede
//
//    private List<Long> managers; // Lista de usuarios que son administradores de la sede
//    private List<Long> trainers; // Lista de usuarios que son entrenadores en la sede
//    private List<Long> members; // Lista de usuarios que son miembros o clientes de la sede
}
