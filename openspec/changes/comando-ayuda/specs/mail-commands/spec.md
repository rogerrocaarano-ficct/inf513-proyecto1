# Spec Delta

## MODIFIED Requirements

### Requirement: Comando desconocido

Cuando ningún caso de uso atiende el comando reconocido, el sistema SHALL descartar el correo y responder al remitente que el comando no existe y que envíe `ayuda[]` para ver los comandos que puede usar. La respuesta MUST NOT listar los comandos del sistema.

#### Scenario: Comando inexistente
- **WHEN** llega el comando `usuarios.borrarTodo[]` y ningún caso de uso lo atiende
- **THEN** el sistema descarta el correo
- **AND** responde al remitente que el comando no existe y que envíe `ayuda[]` para ver los comandos que puede usar
- **AND** la respuesta no lista ningún comando del sistema
