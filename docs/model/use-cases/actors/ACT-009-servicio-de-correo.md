# ACT-009 Servicio de correo

- **Type:** Supporting
- **Kind:** External system

## Description

Servicio externo de correo electrónico por el que el sistema recibe las solicitudes de los usuarios y les envía respuestas y notificaciones. En la prueba de concepto de la primera entrega es el único canal de uso del sistema (CON-001).

## Goals

- Entregar las notificaciones de cuentas de usuario, entregas, designación y programación (UC-001, UC-004, UC-006, UC-007, UC-009)

## Responsibilities

- Entregar al sistema los correos que envían los usuarios
- Entregar a los usuarios los mensajes que el sistema le envía

## Interface

| Characteristic | Value |
| --- | --- |
| Owner | Docente de Tecnología Web (`mail.tecnoweb.org.bo`, CON-006) |
| Interaction | Recepción por POP3 y envío por SMTP, en los puertos por defecto y sin cifrado |
| Direction | Both |
| Availability | TBD |
| Related requirement | NFR-IN-001 |
