package grupo03sc.mailapp.graduationprocesses.domain;

/**
 * Why a graduation process or graduation project request is rejected (UC-003 E1 to E6, UC-004 E1 to E5),
 * with the text that starts the reply.
 */
public enum GraduationRejectionReason {
    MISSING_DATA("Faltan datos"),
    INVALID_DATA("Dato inválido"),
    ATTACHMENT_COUNT("Cantidad de adjuntos incorrecta"),
    MISSING_DOCUMENT("Faltan documentos iniciales"),
    UNRECOGNIZED_DOCUMENT("Adjunto no reconocido"),
    DUPLICATE_DOCUMENT("Documento inicial repetido"),
    INVALID_DOCUMENT("Documento inicial no válido"),
    PROCESS_IN_PROGRESS("El estudiante ya tiene un trámite de titulación en curso"),
    PROCESS_NOT_FOUND("No hay un trámite de titulación en curso"),
    NO_FINAL_GRADE("El trámite de titulación todavía no tiene la calificación final"),
    PROFILE_NOT_ALLOWED("No se admite un perfil de proyecto nuevo"),
    VERSION_PREREQUISITES("Todavía no puede entregar el documento del proyecto"),
    VERSION_NOT_ALLOWED("No se admite una versión nueva"),
    INVALID_FILE("Documento no válido");

    private final String text;

    GraduationRejectionReason(String text) {
        this.text = text;
    }

    public String message(String detail) {
        return detail == null || detail.isBlank() ? text + "." : text + ": " + detail + ".";
    }
}
