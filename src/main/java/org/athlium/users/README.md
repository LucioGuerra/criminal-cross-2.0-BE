# Users Module

Módulo responsable de la gestión de usuarios globales y roles del sistema.

## Responsabilidades
- Gestión de perfiles de usuario
- Asignación de roles
- Configuración de permisos
- Datos personales de usuarios

## Persistencia
- SQL (PostgreSQL)

## Endpoints y permisos
- `GET /api/users` y `GET /api/users/{id}`: requiere `SUPERADMIN`, `ORG_ADMIN` u `ORG_OWNER`
- `POST /api/users`: requiere `SUPERADMIN` o `ORG_ADMIN`
- `GET /api/users/firebase/{uid}`: requiere usuario autenticado
- `PUT /api/users/{id}`: actualizacion completa por ID interno (**endpoint preferido**)
- `PUT /api/users/firebase/{uid}`: actualizacion completa por Firebase UID (legado, mantener por compatibilidad)
- `PUT /api/users/{id}/roles`: actualiza roles por ID interno (**endpoint preferido**)
- `PUT /api/users/firebase/{uid}/roles`: actualiza roles por Firebase UID (legado, mantener por compatibilidad)
- `POST /api/users/firebase/{uid}/headquarters/{headquartersId}`: asigna membresia HQ
- `DELETE /api/users/firebase/{uid}/headquarters/{headquartersId}`: remueve membresia HQ
- `POST /api/users/sync`: **deprecated** para frontend (mantener solo por compatibilidad)

## Reglas de roles
- `SUPERADMIN` puede asignar cualquier rol.
- `ORG_OWNER` puede asignar `ORG_ADMIN`, `PROFESSOR`, `CLIENT`.
- `ORG_ADMIN` puede asignar `PROFESSOR`, `CLIENT`.
- No se permiten listas de roles vacias.
- Solo `SUPERADMIN` puede asignar el rol `SUPERADMIN`.
- Solo `SUPERADMIN` puede modificar usuarios que ya tienen `SUPERADMIN`.

## Reglas de membresia en headquarters
- Auto-asignacion y auto-remocion (usuario sobre si mismo) estan permitidas.
- Para gestionar membresias de otros usuarios se requiere `SUPERADMIN` o perfil admin/owner del mismo scope organizacional (`ORG_ADMIN` / `ORG_OWNER`).
