package grupo03sc.mailapp.graduationprocesses.domain;

/**
 * Why a graduation process request is rejected (UC-003 E1 to E6), with the text that starts the reply.
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
    NO_FINAL_GRADE("El trámite de titulación todavía no tiene la calificación final");

    private final String text;

    GraduationRejectionReason(String text) {
        this.text = text;
    }

    public String message(String detail) {
        return detail == null || detail.isBlank() ? text + "." : text + ": " + detail + ".";
    }
}
