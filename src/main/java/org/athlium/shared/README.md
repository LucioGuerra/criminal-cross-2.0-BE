# Shared Module

Módulo que contiene utilidades, DTOs y excepciones comunes a todos los módulos.

## Responsabilidades
- DTOs comunes (ApiResponse, ErrorResponse)
- Excepciones globales
- Utilidades puras (validaciones, fechas, mapeos)
- Configuraciones compartidas

## Reglas
- No debe contener lógica de negocio
- Solo clases sin dependencias de dominio
- Accesible por todos los módulos