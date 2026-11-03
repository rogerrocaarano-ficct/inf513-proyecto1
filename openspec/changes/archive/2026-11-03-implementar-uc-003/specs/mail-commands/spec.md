# Spec Delta

## MODIFIED Requirements

### Requirement: Adjuntos del comando
El sistema SHALL entregar al comando los archivos adjuntos del correo, con su nombre de archivo decodificado y su contenido, para que el comando los reconozca por su nombre sin que un argumento los nombre. Solo son adjuntos las partes del correo marcadas como adjuntas; las imágenes inline, como las de una firma o las pegadas en el cuerpo, no se entregan.

#### Scenario: Correo con adjunto
- **WHEN** llega un correo con un comando y un archivo adjunto `perfil.pdf`
- **THEN** el comando recibe el adjunto con el nombre `perfil.pdf` y su contenido

#### Scenario: Nombre de archivo codificado
- **WHEN** llega un correo con un adjunto cuyo nombre `Cédula.pdf` viene codificado en el encabezado del correo, en cualquiera de las codificaciones de nombres de archivo de los clientes de correo
- **THEN** el comando recibe el adjunto con el nombre `Cédula.pdf`

#### Scenario: Imagen inline
- **WHEN** llega un correo con un comando, un archivo adjunto `carta.pdf` y una imagen inline `logo.png` en la firma
- **THEN** el comando recibe solo el adjunto `carta.pdf`
