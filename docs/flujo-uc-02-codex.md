# UC-002 — Habilitar una solicitud de titulación

| Campo | Valor |
| --- | --- |
| Estado | Detallado en borrador |
| Iteración | Por confirmar |
| Actor principal | Secretaría de Dirección de Carrera (`administrativo`) |
| Actores de apoyo | Estudiante, que entrega los documentos y recibe avisos |
| Prioridad | Must |
| Significativo para la arquitectura | Sí. Integra recepción de adjuntos, cuentas y apertura del expediente |
| Caso aprobado | [UC-002 en README.md](README.md#casos-de-uso-aprobados) |
| Requisitos funcionales | FR-001, FR-002, FR-026, FR-033, FR-035, FR-051, FR-054 (apertura), FR-055 y FR-056 |

Los flujos siguen [use-case.md](use-case.md) y [REQUIREMENTS.md](REQUIREMENTS.md). Los nombres de comandos, argumentos, palabras clave y ejemplos son propuestas para revisión con el modelo de EA. La sintaxis común y los formatos de datos de cuenta se toman de [USER-GUIDE.md](USER-GUIDE.md).

## Descripción breve

Secretaría verifica la carpeta inicial en oficina, la digitaliza y abre el expediente. Si el estudiante carece de cuenta, la apertura incluye su alta. Secretaría también puede sustituir un documento inicial mientras el trámite está en curso.

## Precondiciones

- Secretaría tiene una cuenta habilitada y envía el comando desde su dirección registrada.
- Para abrir, el estudiante entregó la carta de solicitud, la hoja de vida, la copia de su CI y la boleta de inscripción con Modalidad de Graduación. Secretaría verificó la carpeta.

## Flujo básico

1. Secretaría envía el comando de apertura con los datos de la cuenta del estudiante y los cuatro documentos digitalizados.
2. El sistema autoriza al remitente y valida los argumentos y los adjuntos.
3. El sistema busca la cuenta por correo y registro universitario. Ambos identifican la misma cuenta activa de estudiante, que no tiene otro trámite en curso.
4. El sistema registra la solicitud, abre el expediente sobre esa cuenta y guarda los documentos iniciales.
5. El sistema confirma la apertura a Secretaría y la notifica por correo al estudiante.

## Flujos alternativos

### A1 — Alta de estudiante durante la apertura

En el paso 3, si ni el correo ni el registro universitario están ocupados, el sistema aplica las reglas de creación de cuentas de estudiante del UC-001 y crea la cuenta como parte de la apertura. Continúa en el paso 4. Al finalizar, el estudiante recibe también el aviso de alta previsto en FR-035.

### A2 — Reemplazar un documento inicial

1. Secretaría envía el comando de reemplazo con el identificador del trámite y un adjunto cuyo nombre identifica el tipo de documento.
2. El sistema autoriza al remitente, localiza el trámite y comprueba que sigue en curso. Valida el documento y sustituye el del mismo tipo en el expediente.
3. El sistema confirma el reemplazo a Secretaría.

## Excepciones

| Punto | Condición | Respuesta y resultado |
| --- | --- | --- |
| Apertura 2 / A2.2 | Remitente sin cuenta o permiso, asunto inválido, cantidad incorrecta o datos inválidos | Informa el motivo según la guía de comandos y termina la operación. |
| Apertura 2 / A2.2 | Documento faltante, tipo repetido o no identificable, formato no admitido o tamaño excedido | Indica el problema de los adjuntos y termina la operación. |
| Apertura 3 | Solo uno de los identificadores ya está ocupado, refieren a cuentas distintas o la cuenta coincidente tiene otro rol | Informa que los datos no identifican una cuenta de estudiante utilizable. |
| Apertura 3 | Cuenta desactivada o trámite en curso | Informa la condición que impide la apertura. |
| A2.2 | Trámite inexistente, fuera del alcance de Secretaría o ya cerrado | Rechaza el reemplazo. |

## Postcondiciones

- **Éxito de apertura.** Solicitud registrada y expediente abierto con cuatro documentos y una cuenta de estudiante vinculada. Se emiten los avisos aplicables.
- **Éxito de reemplazo.** El expediente conserva los otros documentos y contiene el documento de sustitución.
- **Fallo.** El rechazo no crea una cuenta ni un expediente parcial y no modifica documentos existentes.

## Requisitos especiales

- NFR-001, NFR-002 y NFR-003. Errores detallados, autorización por rol y alcance, comandos por asunto y adjuntos identificados por una palabra en su nombre.
- NFR-005 y NFR-006. Máximo de 25 MB por PDF y de 1 MB por imagen PNG o JPG/JPEG cuando el comando admita ese formato.

## Contratos base de correo

El destinatario local es `sistema@cpms.local`. El cuerpo se ignora. Se aplican las respuestas comunes de la guía de comandos y los rechazos descritos arriba.

### Abrir trámite

`tramites.abrirTramite[nombres; primerApellido; segundoApellido; ci; registroUniversitario; correo; telefono]`

**Remitente:** Secretaría. **Argumentos:** siete posiciones. El segundo apellido puede quedar vacío. El rol se deriva de la operación (`estudiante`).

| Posición | Argumento | Formato |
| --- | --- | --- |
| 1–3 | Nombres y apellidos | Letras y espacios, según la guía de cuentas |
| 4 | CI | Hasta 7 dígitos, sin complemento |
| 5 | Registro universitario | Hasta 10 dígitos |
| 6 | Correo | Dirección válida |
| 7 | Teléfono | Hasta 8 dígitos |

**Adjuntos:** cuatro, uno por tipo. Convención propuesta para los nombres: `carta.pdf`, `cv.pdf`, `ci.pdf` y `boleta.pdf`. El ejemplo usa PDF.

**Resultado:** confirma a Secretaría que el trámite se abrió e incluye la referencia que se usará en operaciones posteriores. Notifica la apertura al estudiante y, si corresponde, el alta de su cuenta.

```text
tramites.abrirTramite[Ana María; Pérez; ; 1234567; 218001234; ana@cpms.local; 71234567]
```

### Reemplazar documento

`tramites.reemplazarDocumento[referenciaTramite]`

**Remitente:** Secretaría. **Argumentos:** una referencia de trámite obligatoria, devuelta al confirmar la apertura. **Adjuntos:** uno, identificado por la misma convención que en la apertura. **Resultado:** confirma a Secretaría qué documento del expediente se sustituyó.

```text
tramites.reemplazarDocumento[TR-001]
```

## Preguntas abiertas

- [ ] Confirmar con el responsable del modelo los nombres de los comandos, las palabras clave y el formato de referencia del trámite. `TR-001` es un ejemplo.
- [ ] Confirmar con Secretaría qué formatos admite cada tipo documental y si se conserva el archivo sustituido además del registro de cambios.
- [ ] Confirmar con el responsable del modelo cómo se envían los datos cuando la cuenta ya existe. El ejemplo mantiene siete posiciones y la apertura reutiliza la cuenta identificada.
