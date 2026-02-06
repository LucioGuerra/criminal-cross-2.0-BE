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
- `POST /api/users`: requiere `SUPERADMIN` o `ORG_ADMIN`
- `GET /api/users/firebase/{uid}`: requiere usuario autenticado
- `PUT /api/users/firebase/{uid}/roles`: requiere `SUPERADMIN` o `ORG_ADMIN`
- `POST /api/users/sync`: requiere usuario autenticado

## Reglas de roles
- Solo `SUPERADMIN` y `ORG_ADMIN` pueden actualizar roles.
- No se permiten listas de roles vacias.
- Solo `SUPERADMIN` puede asignar el rol `SUPERADMIN`.
- Solo `SUPERADMIN` puede modificar usuarios que ya tienen `SUPERADMIN`.
